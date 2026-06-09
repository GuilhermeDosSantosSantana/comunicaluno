package br.com.comunicaluno.service;

import br.com.comunicaluno.dao.EventoDAO;
import br.com.comunicaluno.model.Evento;
import br.com.comunicaluno.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class EventoService {

    private static final Set<String> EQUIPE_ESCOLAR = Set.of("ADMIN", "COORDENADOR", "PROF");
    private static final Set<String> PUBLICOS_VALIDOS = Set.of("TODOS", "ALUNOS", "PROFESSORES", "COORDENADORES");

    private final EventoDAO eventoDAO;

    public EventoService() {
        this.eventoDAO = new EventoDAO();
    }

    public EventoService(EventoDAO eventoDAO) {
        this.eventoDAO = eventoDAO;
    }

    public void criarEvento(Evento evento, Usuario criadorLogado) {
        exigirEquipeAtiva(criadorLogado);
        if (evento == null) {
            throw new IllegalArgumentException("Dados do evento obrigatórios.");
        }
        if (vazio(evento.getTitulo())) {
            throw new IllegalArgumentException("O título do evento é obrigatório.");
        }
        if (vazio(evento.getDescricao())) {
            throw new IllegalArgumentException("A descrição do evento é obrigatória.");
        }
        if (evento.getDataHora() == null || evento.getDataHora().isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new IllegalArgumentException("A data do evento precisa ser futura.");
        }
        if (!PUBLICOS_VALIDOS.contains(evento.getPublicoAlvo())) {
            throw new IllegalArgumentException("Público do evento inválido.");
        }

        evento.setIdCriador(criadorLogado.getIdUsuario());
        evento.setTitulo(evento.getTitulo().trim());
        evento.setDescricao(evento.getDescricao().trim());
        evento.setLocalEvento(normalizarOpcional(evento.getLocalEvento()));
        evento.setImagemPath(normalizarOpcional(evento.getImagemPath()));
        eventoDAO.salvar(evento);
    }

    public List<Evento> listarProximos(Usuario leitorLogado, int limite) {
        exigirUsuarioAtivo(leitorLogado);
        return eventoDAO.listarProximos(publicoAlvoParaPerfil(leitorLogado.getTipoPerfil()), Math.max(1, limite));
    }

    public int contarProximos(Usuario leitorLogado) {
        exigirUsuarioAtivo(leitorLogado);
        return eventoDAO.contarProximos(publicoAlvoParaPerfil(leitorLogado.getTipoPerfil()));
    }

    public boolean podeArquivar(Evento evento, Usuario operadorLogado) {
        if (!usuarioAtivoValido(operadorLogado) || evento == null || evento.getIdCriador() == null) {
            return false;
        }
        String perfil = operadorLogado.getTipoPerfil();
        if ("ADMIN".equals(perfil) || "COORDENADOR".equals(perfil)) {
            return true;
        }
        return "PROF".equals(perfil) && operadorLogado.getIdUsuario().equals(evento.getIdCriador());
    }

    public void arquivarEvento(int idEvento, Usuario operadorLogado, String motivo) {
        exigirUsuarioAtivo(operadorLogado);
        Evento evento = eventoDAO.buscarPorId(idEvento);
        if (evento == null) {
            throw new IllegalArgumentException("Evento não encontrado.");
        }
        if (!podeArquivar(evento, operadorLogado)) {
            throw new SecurityException("Você não tem permissão para excluir este evento.");
        }
        eventoDAO.arquivarEvento(idEvento, operadorLogado.getIdUsuario(), motivoOuPadrao(motivo));
    }

    private String publicoAlvoParaPerfil(String tipoPerfil) {
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

    private void exigirEquipeAtiva(Usuario usuarioLogado) {
        exigirUsuarioAtivo(usuarioLogado);
        if (!EQUIPE_ESCOLAR.contains(usuarioLogado.getTipoPerfil())) {
            throw new SecurityException("Apenas a equipe escolar pode criar eventos.");
        }
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

    private boolean vazio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private String normalizarOpcional(String valor) {
        return vazio(valor) ? null : valor.trim();
    }

    private String motivoOuPadrao(String motivo) {
        String normalizado = normalizarOpcional(motivo);
        return normalizado == null ? "Excluído pelo usuário." : normalizado;
    }
}
