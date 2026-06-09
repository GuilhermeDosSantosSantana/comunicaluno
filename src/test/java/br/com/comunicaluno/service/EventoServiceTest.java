package br.com.comunicaluno.service;

import br.com.comunicaluno.dao.EventoDAO;
import br.com.comunicaluno.model.Evento;
import br.com.comunicaluno.model.Usuario;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventoServiceTest {

    @Test
    void equipeCriaEventoFuturo() {
        FakeEventoDAO dao = new FakeEventoDAO();
        EventoService service = new EventoService(dao);
        Evento evento = evento("  Semana  ", "  Oficinas  ", "TODOS", LocalDateTime.now().plusDays(2));

        service.criarEvento(evento, usuario(1, "PROF", "ATIVO"));

        assertEquals(1, dao.salvo.getIdCriador());
        assertEquals("Semana", dao.salvo.getTitulo());
        assertEquals("Oficinas", dao.salvo.getDescricao());
    }

    @Test
    void alunoNaoCriaEventoEDataPrecisaSerFutura() {
        EventoService service = new EventoService(new FakeEventoDAO());

        assertThrows(SecurityException.class,
                () -> service.criarEvento(evento("Evento", "Desc", "TODOS", LocalDateTime.now().plusDays(1)),
                        usuario(2, "ALUNO", "ATIVO")));
        assertThrows(IllegalArgumentException.class,
                () -> service.criarEvento(evento("Evento", "Desc", "TODOS", LocalDateTime.now().minusDays(1)),
                        usuario(1, "ADMIN", "ATIVO")));
    }

    @Test
    void listarEventosMapeiaPerfil() {
        FakeEventoDAO dao = new FakeEventoDAO();
        EventoService service = new EventoService(dao);

        service.listarProximos(usuario(3, "ALUNO", "ATIVO"), 3);
        assertEquals("ALUNOS", dao.publico);
        assertEquals(3, dao.limite);
    }

    @Test
    void arquivarEventoRespeitaPerfil() {
        FakeEventoDAO dao = new FakeEventoDAO();
        EventoService service = new EventoService(dao);
        Evento evento = evento("Evento", "Desc", "TODOS", LocalDateTime.now().plusDays(1));
        evento.setIdEvento(20);
        evento.setIdCriador(8);

        assertTrue(service.podeArquivar(evento, usuario(1, "ADMIN", "ATIVO")));
        assertTrue(service.podeArquivar(evento, usuario(2, "COORDENADOR", "ATIVO")));
        assertTrue(service.podeArquivar(evento, usuario(8, "PROF", "ATIVO")));
        assertFalse(service.podeArquivar(evento, usuario(9, "PROF", "ATIVO")));
        assertFalse(service.podeArquivar(evento, usuario(10, "ALUNO", "ATIVO")));

        dao.eventoParaBusca = evento;
        service.arquivarEvento(20, usuario(1, "ADMIN", "ATIVO"), "Duplicado");
        assertEquals(20, dao.ultimoArquivado);
        assertEquals(1, dao.arquivadoPor);

        assertThrows(SecurityException.class, () -> service.arquivarEvento(20, usuario(9, "PROF", "ATIVO"), null));
    }

    private static Evento evento(String titulo, String descricao, String publico, LocalDateTime data) {
        Evento evento = new Evento();
        evento.setTitulo(titulo);
        evento.setDescricao(descricao);
        evento.setPublicoAlvo(publico);
        evento.setDataHora(data);
        return evento;
    }

    private static Usuario usuario(int id, String perfil, String status) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setTipoPerfil(perfil);
        usuario.setStatusConta(status);
        return usuario;
    }

    private static class FakeEventoDAO extends EventoDAO {
        private Evento salvo;
        private String publico;
        private int limite;
        private Evento eventoParaBusca;
        private Integer ultimoArquivado;
        private Integer arquivadoPor;

        @Override
        public void salvar(Evento evento) {
            this.salvo = evento;
        }

        @Override
        public List<Evento> listarProximos(String publicoAlvo, int limite) {
            this.publico = publicoAlvo;
            this.limite = limite;
            return List.of();
        }

        @Override
        public Evento buscarPorId(int idEvento) {
            return eventoParaBusca;
        }

        @Override
        public void arquivarEvento(int idEvento, int idUsuario, String motivo) {
            this.ultimoArquivado = idEvento;
            this.arquivadoPor = idUsuario;
        }
    }
}
