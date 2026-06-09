package br.com.comunicaluno.service;

import br.com.comunicaluno.dao.CursoDAO;
import br.com.comunicaluno.model.Curso;
import br.com.comunicaluno.model.Usuario;

import java.util.List;
import java.util.Set;

public class CursoService {

    private static final Set<String> GESTORES = Set.of("ADMIN", "COORDENADOR");

    private final CursoDAO cursoDAO;

    public CursoService() {
        this.cursoDAO = new CursoDAO();
    }

    public CursoService(CursoDAO cursoDAO) {
        this.cursoDAO = cursoDAO;
    }

    public List<Curso> listarCursosPublicos() {
        return cursoDAO.listarAtivos();
    }

    public List<Curso> listarCursos(Usuario usuarioLogado) {
        exigirUsuarioAtivo(usuarioLogado);
        return cursoDAO.listarAtivos();
    }

    public List<Curso> listarTodosCursos(Usuario usuarioLogado) {
        exigirGestorAtivo(usuarioLogado);
        return cursoDAO.listarTodos();
    }

    public boolean podeGerirCursos(Usuario usuarioLogado) {
        try {
            exigirGestorAtivo(usuarioLogado);
            return true;
        } catch (SecurityException ex) {
            return false;
        }
    }

    public void salvarCurso(Curso curso, Usuario operadorLogado) {
        exigirGestorAtivo(operadorLogado);
        if (curso == null || vazio(curso.getNome()) || vazio(curso.getCodigo())) {
            throw new IllegalArgumentException("Nome e código do curso são obrigatórios.");
        }
        curso.setNome(curso.getNome().trim());
        curso.setCodigo(curso.getCodigo().trim().toUpperCase());
        cursoDAO.salvarOuAtualizar(curso);
    }

    public void alterarStatusCurso(Integer idCurso, boolean ativo, Usuario operadorLogado) {
        exigirGestorAtivo(operadorLogado);
        if (idCurso == null) {
            throw new IllegalArgumentException("Selecione um curso antes de alterar o status.");
        }
        cursoDAO.atualizarStatus(idCurso, ativo);
    }

    private void exigirGestorAtivo(Usuario usuarioLogado) {
        exigirUsuarioAtivo(usuarioLogado);
        if (!GESTORES.contains(usuarioLogado.getTipoPerfil())) {
            throw new SecurityException("Apenas admin ou coordenação podem gerir cursos.");
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
}
