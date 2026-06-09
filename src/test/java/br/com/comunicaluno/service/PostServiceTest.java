package br.com.comunicaluno.service;

import br.com.comunicaluno.dao.PostDAO;
import br.com.comunicaluno.model.Post;
import br.com.comunicaluno.model.PostComentario;
import br.com.comunicaluno.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostServiceTest {

    @Test
    void alunoAtivoPodePublicarPostParaTodosOuAlunos() {
        FakePostDAO dao = new FakePostDAO();
        PostService service = new PostService(dao);
        Usuario aluno = usuario(1, "ALUNO", "ATIVO");

        Post post = new Post("  Olá turma  ", "TODOS");
        service.publicar(post, aluno);

        assertEquals(1, dao.salvo.getIdAutor());
        assertEquals("PUBLICACAO", dao.salvo.getTipoPost());
        assertEquals("Olá turma", dao.salvo.getTexto());
        assertEquals("TODOS", dao.salvo.getPublicoAlvo());
    }

    @Test
    void alunoNaoPublicaAvisoEventoOuParaEquipe() {
        PostService service = new PostService(new FakePostDAO());
        Usuario aluno = usuario(1, "ALUNO", "ATIVO");

        Post aviso = new Post("Texto", "TODOS");
        aviso.setTipoPost("AVISO");
        aviso.setTitulo("Aviso");
        assertThrows(SecurityException.class, () -> service.publicar(aviso, aluno));

        Post reservado = new Post("Texto", "PROFESSORES");
        assertThrows(SecurityException.class, () -> service.publicar(reservado, aluno));

        Post repost = new Post("Texto", "TODOS");
        repost.setTipoPost("REPOST");
        assertThrows(IllegalArgumentException.class, () -> service.publicar(repost, aluno));
    }

    @Test
    void equipePublicaAvisoComTituloObrigatorio() {
        FakePostDAO dao = new FakePostDAO();
        PostService service = new PostService(dao);
        Usuario admin = usuario(2, "ADMIN", "ATIVO");

        Post semTitulo = new Post("Aviso", "TODOS");
        semTitulo.setTipoPost("AVISO");
        assertThrows(IllegalArgumentException.class, () -> service.publicar(semTitulo, admin));

        Post aviso = new Post("  Reunião às 19h  ", "ALUNOS");
        aviso.setTipoPost("AVISO");
        aviso.setTitulo("  Reunião  ");
        service.publicar(aviso, admin);

        assertEquals("AVISO", dao.salvo.getTipoPost());
        assertEquals("Reunião", dao.salvo.getTitulo());
        assertEquals("Reunião às 19h", dao.salvo.getTexto());
    }

    @Test
    void listaFeedMapeiaPerfilEFiltro() {
        FakePostDAO dao = new FakePostDAO();
        PostService service = new PostService(dao);

        service.listarFeed(usuario(1, "ALUNO", "ATIVO"), null, "calculo");
        assertEquals("ALUNOS", dao.publico);
        assertEquals("MEU_PUBLICO", dao.filtro);
        assertEquals("calculo", dao.busca);

        service.listarFeed(usuario(2, "COORDENADOR", "ATIVO"), "TODOS", null);
        assertEquals("QUALQUER", dao.publico);
        assertEquals("TODOS", dao.filtro);
    }

    @Test
    void curtirSalvarEComentarExigemUsuarioAtivo() {
        FakePostDAO dao = new FakePostDAO();
        PostService service = new PostService(dao);
        Usuario aluno = usuario(1, "ALUNO", "ATIVO");

        service.alternarCurtida(10, aluno);
        assertEquals(10, dao.ultimaCurtida);

        dao.existe = true;
        service.alternarSalvo(10, aluno);
        assertEquals(10, dao.ultimoSalvoRemovido);

        service.comentar(10, "  Legal  ", aluno);
        assertEquals("Legal", dao.comentario);

        assertThrows(SecurityException.class, () -> service.alternarCurtida(10, usuario(9, "ALUNO", "PENDENTE")));
    }

    @Test
    void arquivarPostRespeitaPerfilDoOperador() {
        FakePostDAO dao = new FakePostDAO();
        PostService service = new PostService(dao);
        Post postAluno = postExistente(10, 1, "ALUNO");
        Post postProfessor = postExistente(11, 8, "PROF");

        assertTrue(service.podeArquivar(postAluno, usuario(99, "ADMIN", "ATIVO")));
        assertTrue(service.podeArquivar(postAluno, usuario(8, "PROF", "ATIVO")));
        assertTrue(service.podeArquivar(postAluno, usuario(1, "ALUNO", "ATIVO")));
        assertFalse(service.podeArquivar(postProfessor, usuario(9, "PROF", "ATIVO")));
        assertFalse(service.podeArquivar(postAluno, usuario(2, "ALUNO", "ATIVO")));

        dao.postParaBusca = postAluno;
        service.arquivarPost(10, usuario(8, "PROF", "ATIVO"), "Moderação");
        assertEquals(10, dao.ultimoArquivado);
        assertEquals(8, dao.arquivadoPor);
        assertEquals("Moderação", dao.motivoArquivo);

        dao.postParaBusca = postProfessor;
        assertThrows(SecurityException.class, () -> service.arquivarPost(11, usuario(9, "PROF", "ATIVO"), null));
    }

    private static Usuario usuario(int id, String perfil, String status) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setNome("Usuário " + id);
        usuario.setEmail("u" + id + "@escola.com");
        usuario.setTipoPerfil(perfil);
        usuario.setStatusConta(status);
        return usuario;
    }

    private static Post postExistente(int idPost, int idAutor, String perfilAutor) {
        Post post = new Post("Texto", "TODOS");
        post.setIdPost(idPost);
        post.setIdAutor(idAutor);
        post.setPerfilAutor(perfilAutor);
        return post;
    }

    private static class FakePostDAO extends PostDAO {
        private Post salvo;
        private String publico;
        private String filtro;
        private String busca;
        private boolean existe;
        private Integer ultimaCurtida;
        private Integer ultimoSalvoRemovido;
        private String comentario;
        private Post postParaBusca;
        private Integer ultimoArquivado;
        private Integer arquivadoPor;
        private String motivoArquivo;

        @Override
        public void salvar(Post post) {
            this.salvo = post;
        }

        @Override
        public List<Post> listarFeed(String publicoAlvo, String filtroPublico, Integer idUsuarioLogado, String termoBusca) {
            this.publico = publicoAlvo;
            this.filtro = filtroPublico;
            this.busca = termoBusca;
            return List.of();
        }

        @Override
        public boolean curtidaExiste(int idPost, int idUsuario) {
            return false;
        }

        @Override
        public void adicionarCurtida(int idPost, int idUsuario) {
            this.ultimaCurtida = idPost;
        }

        @Override
        public boolean salvoExiste(int idPost, int idUsuario) {
            return existe;
        }

        @Override
        public void removerSalvo(int idPost, int idUsuario) {
            this.ultimoSalvoRemovido = idPost;
        }

        @Override
        public void adicionarComentario(int idPost, int idAutor, String comentario) {
            this.comentario = comentario;
        }

        @Override
        public List<PostComentario> listarComentarios(int idPost) {
            return List.of();
        }

        @Override
        public Post buscarPorId(int idPost, Integer idUsuarioLogado) {
            return postParaBusca;
        }

        @Override
        public void arquivarPost(int idPost, int idUsuario, String motivo) {
            this.ultimoArquivado = idPost;
            this.arquivadoPor = idUsuario;
            this.motivoArquivo = motivo;
        }
    }
}
