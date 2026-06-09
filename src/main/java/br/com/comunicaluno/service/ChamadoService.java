package br.com.comunicaluno.service;

import br.com.comunicaluno.dao.ChamadoDAO;
import br.com.comunicaluno.model.Chamado;
import br.com.comunicaluno.model.ChamadoMensagem;
import br.com.comunicaluno.model.Usuario;

import java.util.List;
import java.util.Set;

public class ChamadoService {

    private static final Set<String> STATUS_VALIDOS = Set.of("ABERTO", "EM_ANALISE", "RESOLVIDO", "FECHADO");
    private static final Set<String> EQUIPE_ATENDIMENTO = Set.of("PROF", "COORDENADOR", "ADMIN");

    private final ChamadoDAO chamadoDAO;

    public ChamadoService() {
        this.chamadoDAO = new ChamadoDAO();
    }

    public ChamadoService(ChamadoDAO chamadoDAO) {
        this.chamadoDAO = chamadoDAO;
    }

    public void abrirChamado(Chamado chamado, Usuario alunoLogado) {
        exigirAlunoAtivo(alunoLogado);

        if (chamado == null) {
            throw new IllegalArgumentException("Dados do chamado obrigatórios.");
        }
        if (chamado.getAssunto() == null || chamado.getAssunto().trim().isEmpty()) {
            throw new IllegalArgumentException("O assunto do chamado é obrigatório.");
        }
        if (chamado.getDescricao() == null || chamado.getDescricao().trim().isEmpty()) {
            throw new IllegalArgumentException("A descrição do chamado é obrigatória.");
        }

        chamado.setIdAluno(alunoLogado.getIdUsuario());
        chamado.setIdResponsavel(null);
        chamado.setStatus("ABERTO");
        chamado.setAssunto(chamado.getAssunto().trim());
        chamado.setDescricao(chamado.getDescricao().trim());
        chamado.setAnexoPath(normalizarTextoOpcional(chamado.getAnexoPath()));

        chamadoDAO.salvar(chamado);
    }

    public List<Chamado> listarChamados(Usuario usuarioLogado) {
        exigirUsuarioAtivo(usuarioLogado);

        if ("ALUNO".equals(usuarioLogado.getTipoPerfil())) {
            return chamadoDAO.listarPorAluno(usuarioLogado.getIdUsuario());
        }
        if (EQUIPE_ATENDIMENTO.contains(usuarioLogado.getTipoPerfil())) {
            return chamadoDAO.listarFilaGeral();
        }

        throw new SecurityException("Perfil sem permissão para consultar chamados.");
    }

    public void assumirChamado(int idChamado, Usuario responsavelLogado) {
        exigirEquipeAtiva(responsavelLogado);
        Chamado chamado = buscarChamadoExistente(idChamado);
        chamadoDAO.assumirChamado(idChamado, responsavelLogado.getIdUsuario());
        chamadoDAO.registrarHistoricoStatus(
                idChamado,
                responsavelLogado.getIdUsuario(),
                chamado.getStatus(),
                "EM_ANALISE",
                "Chamado assumido"
        );
    }

    public void alterarStatus(int idChamado, String novoStatus, Usuario operadorLogado) {
        exigirEquipeAtiva(operadorLogado);
        Chamado chamado = buscarChamadoExistente(idChamado);

        if (!STATUS_VALIDOS.contains(novoStatus)) {
            throw new IllegalArgumentException("Status de chamado inválido.");
        }

        if ("EM_ANALISE".equals(novoStatus)) {
            chamadoDAO.assumirChamado(idChamado, operadorLogado.getIdUsuario());
        } else {
            chamadoDAO.atualizarStatus(idChamado, novoStatus);
        }

        chamadoDAO.registrarHistoricoStatus(
                idChamado,
                operadorLogado.getIdUsuario(),
                chamado.getStatus(),
                novoStatus,
                "Status alterado"
        );
    }

    public void enviarMensagem(int idChamado, String texto, String anexoPath, Usuario autorLogado) {
        exigirUsuarioAtivo(autorLogado);
        Chamado chamado = buscarChamadoExistente(idChamado);
        exigirAcessoAoChamado(chamado, autorLogado);

        String mensagemNormalizada = normalizarTextoOpcional(texto);
        String anexoNormalizado = normalizarTextoOpcional(anexoPath);
        if (mensagemNormalizada == null && anexoNormalizado == null) {
            throw new IllegalArgumentException("Escreva uma mensagem ou anexe um arquivo.");
        }

        ChamadoMensagem mensagem = new ChamadoMensagem();
        mensagem.setIdChamado(idChamado);
        mensagem.setIdAutor(autorLogado.getIdUsuario());
        mensagem.setMensagem(mensagemNormalizada == null ? "Arquivo anexado." : mensagemNormalizada);
        mensagem.setAnexoPath(anexoNormalizado);
        chamadoDAO.adicionarMensagem(mensagem);
    }

    public List<ChamadoMensagem> listarMensagens(int idChamado, Usuario leitorLogado) {
        exigirUsuarioAtivo(leitorLogado);
        Chamado chamado = buscarChamadoExistente(idChamado);
        exigirAcessoAoChamado(chamado, leitorLogado);
        return chamadoDAO.listarMensagens(idChamado);
    }

    public int contarAbertos(Usuario usuarioLogado) {
        exigirUsuarioAtivo(usuarioLogado);
        if (EQUIPE_ATENDIMENTO.contains(usuarioLogado.getTipoPerfil())) {
            return chamadoDAO.contarPorStatus("ABERTO");
        }
        int total = 0;
        for (Chamado chamado : chamadoDAO.listarPorAluno(usuarioLogado.getIdUsuario())) {
            if ("ABERTO".equals(chamado.getStatus()) || "EM_ANALISE".equals(chamado.getStatus())) {
                total++;
            }
        }
        return total;
    }

    public int contarPorStatus(String status, Usuario operadorLogado) {
        exigirEquipeAtiva(operadorLogado);
        if (!STATUS_VALIDOS.contains(status)) {
            throw new IllegalArgumentException("Status de chamado inválido.");
        }
        return chamadoDAO.contarPorStatus(status);
    }

    public boolean podeArquivar(Chamado chamado, Usuario operadorLogado) {
        if (!usuarioAtivoValido(operadorLogado) || chamado == null || chamado.getIdAluno() == null) {
            return false;
        }
        String perfil = operadorLogado.getTipoPerfil();
        if ("ADMIN".equals(perfil) || "COORDENADOR".equals(perfil)) {
            return true;
        }
        if ("ALUNO".equals(perfil)) {
            return operadorLogado.getIdUsuario().equals(chamado.getIdAluno());
        }
        return "PROF".equals(perfil)
                && chamado.getIdResponsavel() != null
                && operadorLogado.getIdUsuario().equals(chamado.getIdResponsavel());
    }

    public void arquivarChamado(int idChamado, Usuario operadorLogado, String motivo) {
        exigirUsuarioAtivo(operadorLogado);
        Chamado chamado = buscarChamadoExistente(idChamado);
        if (!podeArquivar(chamado, operadorLogado)) {
            throw new SecurityException("Você não tem permissão para excluir este chamado.");
        }
        chamadoDAO.arquivarChamado(idChamado, operadorLogado.getIdUsuario(), motivoOuPadrao(motivo));
    }

    private void exigirAlunoAtivo(Usuario usuarioLogado) {
        exigirUsuarioAtivo(usuarioLogado);
        if (!"ALUNO".equals(usuarioLogado.getTipoPerfil())) {
            throw new SecurityException("Apenas alunos podem abrir chamados.");
        }
    }

    private void exigirEquipeAtiva(Usuario usuarioLogado) {
        exigirUsuarioAtivo(usuarioLogado);
        if (!EQUIPE_ATENDIMENTO.contains(usuarioLogado.getTipoPerfil())) {
            throw new SecurityException("Apenas a equipe escolar pode atender chamados.");
        }
    }

    private void exigirUsuarioAtivo(Usuario usuarioLogado) {
        if (usuarioLogado == null || usuarioLogado.getIdUsuario() == null) {
            throw new SecurityException("Utilizador inválido.");
        }
        if (!"ATIVO".equals(usuarioLogado.getStatusConta())) {
            throw new SecurityException("Conta sem permissão ativa.");
        }
        if (usuarioLogado.getTipoPerfil() == null) {
            throw new SecurityException("Perfil inválido.");
        }
    }

    private boolean usuarioAtivoValido(Usuario usuarioLogado) {
        return usuarioLogado != null
                && usuarioLogado.getIdUsuario() != null
                && usuarioLogado.getTipoPerfil() != null
                && "ATIVO".equals(usuarioLogado.getStatusConta());
    }

    private Chamado buscarChamadoExistente(int idChamado) {
        Chamado chamado = chamadoDAO.buscarPorId(idChamado);
        if (chamado == null) {
            throw new IllegalArgumentException("Chamado não encontrado.");
        }
        return chamado;
    }

    private void exigirAcessoAoChamado(Chamado chamado, Usuario usuarioLogado) {
        if ("ALUNO".equals(usuarioLogado.getTipoPerfil())) {
            if (!usuarioLogado.getIdUsuario().equals(chamado.getIdAluno())) {
                throw new SecurityException("Alunos só podem acessar os próprios chamados.");
            }
            return;
        }
        if (!EQUIPE_ATENDIMENTO.contains(usuarioLogado.getTipoPerfil())) {
            throw new SecurityException("Perfil sem permissão para acessar este chamado.");
        }
    }

    private String normalizarTextoOpcional(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return valor.trim();
    }

    private String motivoOuPadrao(String motivo) {
        String normalizado = normalizarTextoOpcional(motivo);
        return normalizado == null ? "Excluído pelo usuário." : normalizado;
    }
}
