package br.com.comunicaluno.service;

import br.com.comunicaluno.dao.PostDAO;
import br.com.comunicaluno.model.Post;
import br.com.comunicaluno.model.PostComentario;
import br.com.comunicaluno.model.Usuario;

import java.util.List;
import java.util.Set;

public class PostService {

    private static final Set<String> TIPOS_VALIDOS = Set.of("PUBLICACAO", "AVISO", "EVENTO");
    private static final Set<String> PUBLICOS_VALIDOS = Set.of("TODOS", "ALUNOS", "PROFESSORES", "COORDENADORES");
    private static final Set<String> FILTROS_VALIDOS = Set.of("MEU_PUBLICO", "TODOS", "ALUNOS", "PROFESSORES", "COORDENADORES");
    private static final Set<String> EQUIPE_ESCOLAR = Set.of("ADMIN", "COORDENADOR", "PROF");

    private final PostDAO postDAO;

    public PostService() {
        this.postDAO = new PostDAO();
    }

    public PostService(PostDAO postDAO) {
        this.postDAO = postDAO;
    }

    public void publicar(Post post, Usuario autorLogado) {
        exigirUsuarioAtivo(autorLogado);
        if (post == null) {
            throw new IllegalArgumentException("Dados da publicação obrigatórios.");
        }

        String tipo = normalizarOpcional(post.getTipoPost());
        if (tipo == null) {
            tipo = "PUBLICACAO";
        }
        if (!TIPOS_VALIDOS.contains(tipo)) {
            throw new IllegalArgumentException("Tipo de publicação inválido.");
        }
        if (!"PUBLICACAO".equals(tipo) && !EQUIPE_ESCOLAR.contains(autorLogado.getTipoPerfil())) {
            throw new SecurityException("Apenas equipe escolar pode publicar avisos ou eventos.");
        }

        String publico = normalizarOpcional(post.getPublicoAlvo());
        if (publico == null) {
            publico = "TODOS";
        }
        if (!PUBLICOS_VALIDOS.contains(publico)) {
            throw new IllegalArgumentException("Público do post inválido.");
        }
        if ("ALUNO".equals(autorLogado.getTipoPerfil())
                && ("PROFESSORES".equals(publico) || "COORDENADORES".equals(publico))) {
            throw new SecurityException("Alunos só podem publicar para todos ou alunos.");
        }

        String texto = normalizarOpcional(post.getTexto());
        String imagem = normalizarOpcional(post.getImagemPath());
        String anexo = normalizarOpcional(post.getAnexoPath());
        if (texto == null && imagem == null && anexo == null) {
            throw new IllegalArgumentException("Escreva algo ou anexe uma imagem/arquivo.");
        }
        if (texto == null) {
            texto = "Arquivo anexado.";
        }

        String titulo = normalizarOpcional(post.getTitulo());
        if (!"PUBLICACAO".equals(tipo) && titulo == null) {
            throw new IllegalArgumentException("Avisos e eventos precisam de título.");
        }

        post.setIdAutor(autorLogado.getIdUsuario());
        post.setTipoPost(tipo);
        post.setPublicoAlvo(publico);
        post.setTexto(texto);
        post.setTitulo(titulo);
        post.setImagemPath(imagem);
        post.setAnexoPath(anexo);
        postDAO.salvar(post);
    }

    public List<Post> listarFeed(Usuario leitorLogado, String filtroPublico, String termoBusca) {
        exigirUsuarioAtivo(leitorLogado);
        String filtro = normalizarFiltro(filtroPublico);
        return postDAO.listarFeed(publicoAlvoParaPerfil(leitorLogado.getTipoPerfil()), filtro, leitorLogado.getIdUsuario(), termoBusca);
    }

    public void comentar(int idPost, String comentario, Usuario autorLogado) {
        exigirUsuarioAtivo(autorLogado);
        String texto = normalizarOpcional(comentario);
        if (texto == null) {
            throw new IllegalArgumentException("O comentário não pode estar vazio.");
        }
        postDAO.adicionarComentario(idPost, autorLogado.getIdUsuario(), texto);
    }

    public List<PostComentario> listarComentarios(int idPost, Usuario leitorLogado) {
        exigirUsuarioAtivo(leitorLogado);
        return postDAO.listarComentarios(idPost);
    }

    public Post buscarPublicacao(int idPost, Usuario leitorLogado) {
        exigirUsuarioAtivo(leitorLogado);
        Post post = postDAO.buscarPorId(idPost, leitorLogado.getIdUsuario());
        if (post == null) {
            throw new IllegalArgumentException("Publicação não encontrada.");
        }
        return post;
    }

    public void alternarCurtida(int idPost, Usuario usuarioLogado) {
        exigirUsuarioAtivo(usuarioLogado);
        if (postDAO.curtidaExiste(idPost, usuarioLogado.getIdUsuario())) {
            postDAO.removerCurtida(idPost, usuarioLogado.getIdUsuario());
        } else {
            postDAO.adicionarCurtida(idPost, usuarioLogado.getIdUsuario());
        }
    }

    public void alternarSalvo(int idPost, Usuario usuarioLogado) {
        exigirUsuarioAtivo(usuarioLogado);
        if (postDAO.salvoExiste(idPost, usuarioLogado.getIdUsuario())) {
            postDAO.removerSalvo(idPost, usuarioLogado.getIdUsuario());
        } else {
            postDAO.adicionarSalvo(idPost, usuarioLogado.getIdUsuario());
        }
    }

    public int contarPostsVisiveis(Usuario usuarioLogado) {
        exigirUsuarioAtivo(usuarioLogado);
        return postDAO.contarPostsVisiveis(publicoAlvoParaPerfil(usuarioLogado.getTipoPerfil()));
    }

    public boolean podeArquivar(Post post, Usuario operadorLogado) {
        if (!usuarioAtivoValido(operadorLogado) || post == null || post.getIdAutor() == null) {
            return false;
        }
        String perfil = operadorLogado.getTipoPerfil();
        if ("ADMIN".equals(perfil) || "COORDENADOR".equals(perfil)) {
            return true;
        }
        if (operadorLogado.getIdUsuario().equals(post.getIdAutor())) {
            return true;
        }
        return "PROF".equals(perfil) && "ALUNO".equals(post.getPerfilAutor());
    }

    public void arquivarPost(int idPost, Usuario operadorLogado, String motivo) {
        exigirUsuarioAtivo(operadorLogado);
        Post post = postDAO.buscarPorId(idPost, operadorLogado.getIdUsuario());
        if (post == null) {
            throw new IllegalArgumentException("Publicação não encontrada.");
        }
        if (!podeArquivar(post, operadorLogado)) {
            throw new SecurityException("Você não tem permissão para excluir esta publicação.");
        }
        postDAO.arquivarPost(idPost, operadorLogado.getIdUsuario(), motivoOuPadrao(motivo));
    }

    public String publicoAlvoParaPerfil(String tipoPerfil) {
        if ("ADMIN".equals(tipoPerfil) || "COORDENADOR".equals(tipoPerfil)) {
            return "QUALQUER";
        }
        if ("PROF".equals(tipoPerfil)) {
            return "PROFESSORES";
        }
        if ("ALUNO".equals(tipoPerfil)) {
            return "ALUNOS";
        }
        return "NENHUM";
    }

    private String normalizarFiltro(String filtroPublico) {
        String filtro = normalizarOpcional(filtroPublico);
        if (filtro == null) {
            return "MEU_PUBLICO";
        }
        if (!FILTROS_VALIDOS.contains(filtro)) {
            throw new IllegalArgumentException("Filtro de público inválido.");
        }
        return filtro;
    }

    private void exigirUsuarioAtivo(Usuario usuarioLogado) {
        if (usuarioLogado == null || usuarioLogado.getIdUsuario() == null || usuarioLogado.getTipoPerfil() == null) {
            throw new SecurityException("Usuário inválido.");
        }
        if (!"ATIVO".equals(usuarioLogado.getStatusConta())) {
            throw new SecurityException("Conta sem permissão ativa.");
        }
    }

    private boolean usuarioAtivoValido(Usuario usuarioLogado) {
        return usuarioLogado != null
                && usuarioLogado.getIdUsuario() != null
                && usuarioLogado.getTipoPerfil() != null
                && "ATIVO".equals(usuarioLogado.getStatusConta());
    }

    private String motivoOuPadrao(String motivo) {
        String normalizado = normalizarOpcional(motivo);
        return normalizado == null ? "Excluído pelo usuário." : normalizado;
    }

    private String normalizarOpcional(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return valor.trim();
    }
}
