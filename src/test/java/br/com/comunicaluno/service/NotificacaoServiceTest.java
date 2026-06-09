package br.com.comunicaluno.service;

import br.com.comunicaluno.dao.NotificacaoDAO;
import br.com.comunicaluno.model.Notificacao;
import br.com.comunicaluno.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificacaoServiceTest {

    @Test
    void criaNotificacaoValida() {
        FakeNotificacaoDAO dao = new FakeNotificacaoDAO();
        NotificacaoService service = new NotificacaoService(dao);

        service.notificarUsuario(1, "  Novo aviso  ", "  Mensagem  ", "FEED", "FEED");

        assertEquals(1, dao.salva.getIdUsuario());
        assertEquals("Novo aviso", dao.salva.getTitulo());
        assertFalse(dao.salva.isLida());
    }

    @Test
    void rejeitaNotificacaoInvalida() {
        NotificacaoService service = new NotificacaoService(new FakeNotificacaoDAO());

        assertThrows(IllegalArgumentException.class,
                () -> service.notificarUsuario(1, "", "Mensagem", "FEED", null));
    }

    @Test
    void listaEContaNotificacoesDeUsuarioAtivo() {
        FakeNotificacaoDAO dao = new FakeNotificacaoDAO();
        NotificacaoService service = new NotificacaoService(dao);
        Usuario usuario = usuario(4);

        service.listarRecentes(usuario, 5);
        assertEquals(4, dao.ultimoUsuarioListagem);
        assertEquals(5, dao.limite);

        service.contarNaoLidas(usuario);
        assertEquals(4, dao.ultimoUsuarioContagem);
    }

    private static Usuario usuario(int id) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setStatusConta("ATIVO");
        return usuario;
    }

    private static class FakeNotificacaoDAO extends NotificacaoDAO {
        private Notificacao salva;
        private int ultimoUsuarioListagem;
        private int ultimoUsuarioContagem;
        private int limite;

        @Override
        public void salvar(Notificacao notificacao) {
            this.salva = notificacao;
        }

        @Override
        public List<Notificacao> listarRecentes(int idUsuario, int limite) {
            this.ultimoUsuarioListagem = idUsuario;
            this.limite = limite;
            return List.of();
        }

        @Override
        public int contarNaoLidas(int idUsuario) {
            this.ultimoUsuarioContagem = idUsuario;
            return 0;
        }
    }
}
