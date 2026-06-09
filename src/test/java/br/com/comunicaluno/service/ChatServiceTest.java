package br.com.comunicaluno.service;

import br.com.comunicaluno.dao.ChatDAO;
import br.com.comunicaluno.dao.CursoDAO;
import br.com.comunicaluno.dao.DisciplinaDAO;
import br.com.comunicaluno.dao.UsuarioDAO;
import br.com.comunicaluno.model.Conversa;
import br.com.comunicaluno.model.ConversaMensagem;
import br.com.comunicaluno.model.ConversaParticipante;
import br.com.comunicaluno.model.Curso;
import br.com.comunicaluno.model.Disciplina;
import br.com.comunicaluno.model.GrupoAcademico;
import br.com.comunicaluno.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChatServiceTest {

    @Test
    void alunoIniciaConversaPrivadaComOutroAluno() {
        Fakes fakes = new Fakes();
        ChatService service = fakes.service();

        Conversa conversa = service.iniciarConversaPrivada(2, usuario(1, "ALUNO"));

        assertEquals("PRIVADA", conversa.getTipo());
        assertEquals(30, conversa.getIdConversa());
        assertEquals(List.of("30:1:CRIADOR", "30:2:MEMBRO"), fakes.chat.participantes);
    }

    @Test
    void alunoNaoIniciaPrivadoComProfessor() {
        Fakes fakes = new Fakes();
        ChatService service = fakes.service();

        assertThrows(IllegalArgumentException.class,
                () -> service.iniciarConversaPrivada(8, usuario(1, "ALUNO")));
    }

    @Test
    void adminCriaGrupoDeMateriaComProfessorEAlunos() {
        Fakes fakes = new Fakes();
        ChatService service = fakes.service();
        GrupoAcademico grupo = new GrupoAcademico();
        grupo.setTipo("GRUPO_MATERIA");
        grupo.setIdDisciplina(4);
        grupo.setIdProfessorResponsavel(8);
        grupo.setIdsAlunos(List.of(1, 2));

        Conversa conversa = service.criarGrupo(grupo, usuario(99, "ADMIN"));

        assertEquals("GRUPO_MATERIA", conversa.getTipo());
        assertEquals("Matéria - Programação", conversa.getNome());
        assertEquals(4, conversa.getIdDisciplina());
        assertEquals(List.of("30:99:CRIADOR", "30:8:PROF_RESPONSAVEL", "30:1:MEMBRO", "30:2:MEMBRO"),
                fakes.chat.participantes);
    }

    @Test
    void professorResponsavelAdicionaAlunoMasAlunoNaoGerenciaGrupo() {
        Fakes fakes = new Fakes();
        ChatService service = fakes.service();
        fakes.chat.conversaParaBusca = conversaGrupo();

        service.adicionarAluno(30, 2, usuario(8, "PROF"));
        assertEquals("30:2:MEMBRO", fakes.chat.participantes.get(0));

        assertThrows(SecurityException.class, () -> service.adicionarAluno(30, 2, usuario(1, "ALUNO")));
    }

    private static Conversa conversaGrupo() {
        Conversa conversa = new Conversa();
        conversa.setIdConversa(30);
        conversa.setTipo("GRUPO_MATERIA");
        conversa.setIdDisciplina(4);
        conversa.setIdProfessorResponsavel(8);
        conversa.setNome("Programação");
        return conversa;
    }

    private static Usuario usuario(int id, String perfil) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setNome("Usuário " + id);
        usuario.setEmail("usuario" + id + "@escola.com");
        usuario.setTipoPerfil(perfil);
        usuario.setStatusConta("ATIVO");
        return usuario;
    }

    private static Curso curso() {
        Curso curso = new Curso();
        curso.setIdCurso(3);
        curso.setNome("Engenharia");
        curso.setCodigo("ENG");
        return curso;
    }

    private static Disciplina disciplina() {
        Disciplina disciplina = new Disciplina();
        disciplina.setIdDisciplina(4);
        disciplina.setNome("Programação");
        disciplina.setCodigo("POO");
        return disciplina;
    }

    private static class Fakes {
        private final FakeChatDAO chat = new FakeChatDAO();
        private final FakeUsuarioDAO usuarios = new FakeUsuarioDAO();
        private final FakeCursoDAO cursos = new FakeCursoDAO();
        private final FakeDisciplinaDAO disciplinas = new FakeDisciplinaDAO();

        private ChatService service() {
            return new ChatService(chat, usuarios, cursos, disciplinas);
        }
    }

    private static class FakeChatDAO extends ChatDAO {
        private final List<String> participantes = new ArrayList<>();
        private Conversa conversaParaBusca;

        @Override
        public void criarConversa(Conversa conversa) {
            conversa.setIdConversa(30);
            this.conversaParaBusca = conversa;
        }

        @Override
        public Conversa buscarPrivadaEntre(int idUsuarioA, int idUsuarioB) {
            return null;
        }

        @Override
        public Conversa buscarPorId(int idConversa) {
            return conversaParaBusca;
        }

        @Override
        public void adicionarParticipante(int idConversa, int idUsuario, String papel, Integer addedBy) {
            participantes.add(idConversa + ":" + idUsuario + ":" + papel);
        }

        @Override
        public boolean participanteAtivo(int idConversa, int idUsuario) {
            return true;
        }

        @Override
        public void enviarMensagem(ConversaMensagem mensagem) {
        }

        @Override
        public List<ConversaMensagem> listarMensagens(int idConversa) {
            return List.of();
        }

        @Override
        public List<ConversaParticipante> listarParticipantes(int idConversa) {
            return List.of();
        }
    }

    private static class FakeUsuarioDAO extends UsuarioDAO {
        private final Map<Integer, Usuario> usuarios = new HashMap<>();

        private FakeUsuarioDAO() {
            usuarios.put(1, usuario(1, "ALUNO"));
            usuarios.put(2, usuario(2, "ALUNO"));
            usuarios.put(8, usuario(8, "PROF"));
            usuarios.put(99, usuario(99, "ADMIN"));
        }

        @Override
        public Usuario buscarPorId(int idUsuario) {
            return usuarios.get(idUsuario);
        }

        @Override
        public List<Usuario> listarAtivosPorPerfil(String tipoPerfil) {
            return usuarios.values().stream()
                    .filter(usuario -> tipoPerfil.equals(usuario.getTipoPerfil()))
                    .toList();
        }
    }

    private static class FakeCursoDAO extends CursoDAO {
        @Override
        public Curso buscarPorId(int idCurso) {
            return idCurso == 3 ? curso() : null;
        }
    }

    private static class FakeDisciplinaDAO extends DisciplinaDAO {
        @Override
        public Disciplina buscarPorId(int idDisciplina) {
            return idDisciplina == 4 ? disciplina() : null;
        }
    }
}
