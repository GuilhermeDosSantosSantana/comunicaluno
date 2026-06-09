package br.com.comunicaluno.service;

import br.com.comunicaluno.dao.DisciplinaDAO;
import br.com.comunicaluno.model.Disciplina;
import br.com.comunicaluno.model.Usuario;

import java.util.List;
import java.util.Set;

public class DisciplinaService {

    private static final Set<String> EQUIPE_ESCOLAR = Set.of("ADMIN", "COORDENADOR", "PROF");
    private final DisciplinaDAO disciplinaDAO;

    public DisciplinaService() {
        this.disciplinaDAO = new DisciplinaDAO();
    }

    public DisciplinaService(DisciplinaDAO disciplinaDAO) {
        this.disciplinaDAO = disciplinaDAO;
    }

    public List<Disciplina> listarParaUsuario(Usuario usuarioLogado) {
        exigirUsuarioAtivo(usuarioLogado);
        return disciplinaDAO.listarParaUsuario(usuarioLogado.getIdUsuario());
    }

    public int contarDisponiveis(Usuario usuarioLogado) {
        exigirUsuarioAtivo(usuarioLogado);
        return disciplinaDAO.contarDisponiveis();
    }

    public List<Disciplina> listarCatalogo(Usuario usuarioLogado) {
        exigirUsuarioAtivo(usuarioLogado);
        return disciplinaDAO.listarTodas();
    }

    public void salvarDisciplina(Disciplina disciplina, Usuario operadorLogado) {
        exigirEquipeAtiva(operadorLogado);
        if (disciplina == null || vazio(disciplina.getNome()) || vazio(disciplina.getCodigo())) {
            throw new IllegalArgumentException("Nome e código da disciplina são obrigatórios.");
        }
        disciplina.setNome(disciplina.getNome().trim());
        disciplina.setCodigo(disciplina.getCodigo().trim().toUpperCase());
        disciplina.setProfessorNome(normalizarOpcional(disciplina.getProfessorNome()));
        disciplina.setCapaPath(normalizarOpcional(disciplina.getCapaPath()));
        disciplinaDAO.salvarOuAtualizar(disciplina);
    }

    private void exigirEquipeAtiva(Usuario usuarioLogado) {
        exigirUsuarioAtivo(usuarioLogado);
        if (!EQUIPE_ESCOLAR.contains(usuarioLogado.getTipoPerfil())) {
            throw new SecurityException("Apenas a equipe escolar pode gerir disciplinas.");
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

    private boolean vazio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private String normalizarOpcional(String valor) {
        return vazio(valor) ? null : valor.trim();
    }
}
