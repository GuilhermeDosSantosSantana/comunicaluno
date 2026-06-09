package br.com.comunicaluno.service;

import br.com.comunicaluno.dao.DisciplinaDAO;
import br.com.comunicaluno.model.Disciplina;
import br.com.comunicaluno.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DisciplinaServiceTest {

    @Test
    void equipeCadastraDisciplinaNormalizada() {
        FakeDisciplinaDAO dao = new FakeDisciplinaDAO();
        DisciplinaService service = new DisciplinaService(dao);
        Disciplina disciplina = new Disciplina();
        disciplina.setNome("  Física  ");
        disciplina.setCodigo(" fis-1 ");

        service.salvarDisciplina(disciplina, usuario(1, "COORDENADOR", "ATIVO"));

        assertEquals("Física", dao.salva.getNome());
        assertEquals("FIS-1", dao.salva.getCodigo());
    }

    @Test
    void alunoNaoGereDisciplina() {
        DisciplinaService service = new DisciplinaService(new FakeDisciplinaDAO());
        Disciplina disciplina = new Disciplina();
        disciplina.setNome("Física");
        disciplina.setCodigo("FIS");

        assertThrows(SecurityException.class,
                () -> service.salvarDisciplina(disciplina, usuario(2, "ALUNO", "ATIVO")));
    }

    @Test
    void listaDisciplinasParaUsuarioAtivo() {
        FakeDisciplinaDAO dao = new FakeDisciplinaDAO();
        DisciplinaService service = new DisciplinaService(dao);

        service.listarParaUsuario(usuario(7, "ALUNO", "ATIVO"));
        assertEquals(7, dao.ultimoUsuario);
    }

    private static Usuario usuario(int id, String perfil, String status) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setTipoPerfil(perfil);
        usuario.setStatusConta(status);
        return usuario;
    }

    private static class FakeDisciplinaDAO extends DisciplinaDAO {
        private Disciplina salva;
        private int ultimoUsuario;

        @Override
        public void salvarOuAtualizar(Disciplina disciplina) {
            this.salva = disciplina;
        }

        @Override
        public List<Disciplina> listarParaUsuario(int idUsuario) {
            this.ultimoUsuario = idUsuario;
            return List.of();
        }
    }
}
