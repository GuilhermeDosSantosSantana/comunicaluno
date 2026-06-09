package br.com.comunicaluno.service;

import br.com.comunicaluno.dao.NotificacaoDAO;
import br.com.comunicaluno.model.Notificacao;
import br.com.comunicaluno.model.Usuario;

import java.util.List;

public class NotificacaoService {

    private final NotificacaoDAO notificacaoDAO;

    public NotificacaoService() {
        this.notificacaoDAO = new NotificacaoDAO();
    }

    public NotificacaoService(NotificacaoDAO notificacaoDAO) {
        this.notificacaoDAO = notificacaoDAO;
    }

    public void notificarUsuario(Integer idUsuario, String titulo, String mensagem, String tipo, String destino) {
        if (vazio(titulo) || vazio(mensagem) || vazio(tipo)) {
            throw new IllegalArgumentException("Título, mensagem e tipo da notificação são obrigatórios.");
        }
        Notificacao notificacao = new Notificacao();
        notificacao.setIdUsuario(idUsuario);
        notificacao.setTitulo(titulo.trim());
        notificacao.setMensagem(mensagem.trim());
        notificacao.setTipo(tipo.trim());
        notificacao.setDestino(vazio(destino) ? null : destino.trim());
        notificacao.setLida(false);
        notificacaoDAO.salvar(notificacao);
    }

    public List<Notificacao> listarRecentes(Usuario usuarioLogado, int limite) {
        exigirUsuarioAtivo(usuarioLogado);
        return notificacaoDAO.listarRecentes(usuarioLogado.getIdUsuario(), Math.max(1, limite));
    }

    public int contarNaoLidas(Usuario usuarioLogado) {
        exigirUsuarioAtivo(usuarioLogado);
        return notificacaoDAO.contarNaoLidas(usuarioLogado.getIdUsuario());
    }

    public void marcarTodasComoLidas(Usuario usuarioLogado) {
        exigirUsuarioAtivo(usuarioLogado);
        notificacaoDAO.marcarTodasComoLidas(usuarioLogado.getIdUsuario());
    }

    private void exigirUsuarioAtivo(Usuario usuarioLogado) {
        if (usuarioLogado == null || usuarioLogado.getIdUsuario() == null) {
            throw new SecurityException("Usuário inválido.");
        }
        if (!"ATIVO".equals(usuarioLogado.getStatusConta())) {
            throw new SecurityException("Conta sem permissão ativa.");
        }
    }

    private boolean vazio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
