package br.com.comunicaluno.service;

import br.com.comunicaluno.dao.CursoDAO;
import br.com.comunicaluno.model.Curso;
import br.com.comunicaluno.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CursoServiceTest {

    @Test
    void listaCursosPublicosSemUsuarioLogado() {
        FakeCursoDAO dao = new FakeCursoDAO();
        CursoService service = new CursoService(dao);

        assertEquals(1, service.listarCursosPublicos().size());
    }

    @Test
    void apenasAdminOuCoordenadorSalvaCurso() {
        FakeCursoDAO dao = new FakeCursoDAO();
        CursoService service = new CursoService(dao);
        Curso curso = curso("  Engenharia  ", " eng ");

        service.salvarCurso(curso, usuario(1, "ADMIN"));

        assertEquals("Engenharia", dao.salvo.getNome());
        assertEquals("ENG", dao.salvo.getCodigo());
        assertThrows(SecurityException.class, () -> service.salvarCurso(curso("ADS", "ADS"), usuario(2, "PROF")));
    }

    private static Curso curso(String nome, String codigo) {
        Curso curso = new Curso();
        curso.setNome(nome);
        curso.setCodigo(codigo);
        return curso;
    }

    private static Usuario usuario(int id, String perfil) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setTipoPerfil(perfil);
        usuario.setStatusConta("ATIVO");
        return usuario;
    }

    private static class FakeCursoDAO extends CursoDAO {
        private Curso salvo;

        @Override
        public List<Curso> listarAtivos() {
            return List.of(curso("Engenharia", "ENG"));
        }

        @Override
        public void salvarOuAtualizar(Curso curso) {
            this.salvo = curso;
        }
    }
}
