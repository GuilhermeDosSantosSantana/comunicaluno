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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ChatService {

    private static final Set<String> GESTORES = Set.of("ADMIN", "COORDENADOR");
    private static final Set<String> TIPOS_GRUPO_VALIDOS = Set.of("GRUPO_MATERIA", "GRUPO_CURSO");

    private final ChatDAO chatDAO;
    private final UsuarioDAO usuarioDAO;
    private final CursoDAO cursoDAO;
    private final DisciplinaDAO disciplinaDAO;

    public ChatService() {
        this(new ChatDAO(), new UsuarioDAO(), new CursoDAO(), new DisciplinaDAO());
    }

    public ChatService(ChatDAO chatDAO, UsuarioDAO usuarioDAO, CursoDAO cursoDAO, DisciplinaDAO disciplinaDAO) {
        this.chatDAO = chatDAO;
        this.usuarioDAO = usuarioDAO;
        this.cursoDAO = cursoDAO;
        this.disciplinaDAO = disciplinaDAO;
    }

    public List<Conversa> listarConversas(Usuario usuarioLogado) {
        exigirUsuarioAtivo(usuarioLogado);
        return chatDAO.listarConversasParaUsuario(usuarioLogado.getIdUsuario(), GESTORES.contains(usuarioLogado.getTipoPerfil()));
    }

    public Conversa iniciarConversaPrivada(int idOutroAluno, Usuario alunoLogado) {
        exigirAlunoAtivo(alunoLogado);
        if (alunoLogado.getIdUsuario().equals(idOutroAluno)) {
            throw new IllegalArgumentException("Escolha outro aluno para iniciar a conversa.");
        }
        Usuario outroAluno = buscarUsuarioAtivoPorPerfil(idOutroAluno, "ALUNO");

        Conversa existente = chatDAO.buscarPrivadaEntre(alunoLogado.getIdUsuario(), idOutroAluno);
        if (existente != null) {
            return existente;
        }

        Conversa conversa = new Conversa();
        conversa.setTipo("PRIVADA");
        conversa.setNome(alunoLogado.getNome() + " e " + outroAluno.getNome());
        conversa.setCreatedBy(alunoLogado.getIdUsuario());
        chatDAO.criarConversa(conversa);
        chatDAO.adicionarParticipante(conversa.getIdConversa(), alunoLogado.getIdUsuario(), "CRIADOR", alunoLogado.getIdUsuario());
        chatDAO.adicionarParticipante(conversa.getIdConversa(), outroAluno.getIdUsuario(), "MEMBRO", alunoLogado.getIdUsuario());
        return conversa;
    }

    public Conversa criarGrupo(GrupoAcademico grupo, Usuario operadorLogado) {
        exigirGestorAtivo(operadorLogado);
        if (grupo == null || vazio(grupo.getTipo()) || !TIPOS_GRUPO_VALIDOS.contains(grupo.getTipo())) {
            throw new IllegalArgumentException("Tipo de grupo inválido.");
        }

        Usuario professor = buscarUsuarioAtivoPorPerfil(exigirId(grupo.getIdProfessorResponsavel(), "Professor responsável obrigatório."), "PROF");
        String nomeGrupo = normalizarOpcional(grupo.getNome());

        Conversa conversa = new Conversa();
        conversa.setTipo(grupo.getTipo());
        conversa.setIdProfessorResponsavel(professor.getIdUsuario());
        conversa.setCreatedBy(operadorLogado.getIdUsuario());

        if ("GRUPO_CURSO".equals(grupo.getTipo())) {
            Curso curso = cursoDAO.buscarPorId(exigirId(grupo.getIdCurso(), "Curso obrigatório."));
            if (curso == null) {
                throw new IllegalArgumentException("Curso não encontrado.");
            }
            conversa.setIdCurso(curso.getIdCurso());
            conversa.setNome(nomeGrupo == null ? "Curso - " + curso.getNome() : nomeGrupo);
        } else {
            Disciplina disciplina = disciplinaDAO.buscarPorId(exigirId(grupo.getIdDisciplina(), "Matéria obrigatória."));
            if (disciplina == null) {
                throw new IllegalArgumentException("Matéria não encontrada.");
            }
            conversa.setIdDisciplina(disciplina.getIdDisciplina());
            conversa.setNome(nomeGrupo == null ? "Matéria - " + disciplina.getNome() : nomeGrupo);
        }

        chatDAO.criarConversa(conversa);
        chatDAO.adicionarParticipante(conversa.getIdConversa(), operadorLogado.getIdUsuario(), "CRIADOR", operadorLogado.getIdUsuario());
        chatDAO.adicionarParticipante(conversa.getIdConversa(), professor.getIdUsuario(), "PROF_RESPONSAVEL", operadorLogado.getIdUsuario());

        for (Integer idAluno : idsUnicos(grupo.getIdsAlunos())) {
            Usuario aluno = buscarUsuarioAtivoPorPerfil(idAluno, "ALUNO");
            chatDAO.adicionarParticipante(conversa.getIdConversa(), aluno.getIdUsuario(), "MEMBRO", operadorLogado.getIdUsuario());
        }

        return conversa;
    }

    public void adicionarAluno(int idConversa, int idAluno, Usuario operadorLogado) {
        Conversa conversa = buscarConversaExistente(idConversa);
        exigirPermissaoGerenciarMembros(conversa, operadorLogado);
        Usuario aluno = buscarUsuarioAtivoPorPerfil(idAluno, "ALUNO");
        chatDAO.adicionarParticipante(idConversa, aluno.getIdUsuario(), "MEMBRO", operadorLogado.getIdUsuario());
    }

    public void removerAluno(int idConversa, int idAluno, Usuario operadorLogado) {
        Conversa conversa = buscarConversaExistente(idConversa);
        exigirPermissaoGerenciarMembros(conversa, operadorLogado);
        Usuario aluno = buscarUsuarioAtivoPorPerfil(idAluno, "ALUNO");
        chatDAO.removerParticipante(idConversa, aluno.getIdUsuario(), operadorLogado.getIdUsuario());
    }

    public void enviarMensagem(int idConversa, String texto, String anexoPath, Usuario autorLogado) {
        exigirUsuarioAtivo(autorLogado);
        Conversa conversa = buscarConversaExistente(idConversa);
        exigirAcessoConversa(conversa, autorLogado);

        String mensagemNormalizada = normalizarOpcional(texto);
        String anexoNormalizado = normalizarOpcional(anexoPath);
        if (mensagemNormalizada == null && anexoNormalizado == null) {
            throw new IllegalArgumentException("Escreva uma mensagem ou anexe um arquivo.");
        }

        ConversaMensagem mensagem = new ConversaMensagem();
        mensagem.setIdConversa(idConversa);
        mensagem.setIdAutor(autorLogado.getIdUsuario());
        mensagem.setMensagem(mensagemNormalizada == null ? "Arquivo anexado." : mensagemNormalizada);
        mensagem.setAnexoPath(anexoNormalizado);
        chatDAO.enviarMensagem(mensagem);
    }

    public List<ConversaMensagem> listarMensagens(int idConversa, Usuario leitorLogado) {
        exigirUsuarioAtivo(leitorLogado);
        Conversa conversa = buscarConversaExistente(idConversa);
        exigirAcessoConversa(conversa, leitorLogado);
        return chatDAO.listarMensagens(idConversa);
    }

    public List<ConversaParticipante> listarParticipantes(int idConversa, Usuario leitorLogado) {
        exigirUsuarioAtivo(leitorLogado);
        Conversa conversa = buscarConversaExistente(idConversa);
        exigirAcessoConversa(conversa, leitorLogado);
        return chatDAO.listarParticipantes(idConversa);
    }

    public boolean podeArquivarGrupo(Conversa conversa, Usuario operadorLogado) {
        if (!usuarioAtivoValido(operadorLogado) || conversa == null || "PRIVADA".equals(conversa.getTipo())) {
            return false;
        }
        if (GESTORES.contains(operadorLogado.getTipoPerfil())) {
            return true;
        }
        return "PROF".equals(operadorLogado.getTipoPerfil())
                && conversa.getIdProfessorResponsavel() != null
                && operadorLogado.getIdUsuario().equals(conversa.getIdProfessorResponsavel());
    }

    public void arquivarGrupo(int idConversa, Usuario operadorLogado, String motivo) {
        exigirUsuarioAtivo(operadorLogado);
        Conversa conversa = buscarConversaExistente(idConversa);
        if (!podeArquivarGrupo(conversa, operadorLogado)) {
            throw new SecurityException("Você não tem permissão para excluir este grupo.");
        }
        chatDAO.arquivarConversa(idConversa, operadorLogado.getIdUsuario(), motivoOuPadrao(motivo));
    }

    public List<Usuario> listarAlunosAtivos(Usuario operadorLogado) {
        exigirUsuarioAtivo(operadorLogado);
        return usuarioDAO.listarAtivosPorPerfil("ALUNO");
    }

    public List<Usuario> listarProfessoresAtivos(Usuario operadorLogado) {
        exigirUsuarioAtivo(operadorLogado);
        return usuarioDAO.listarAtivosPorPerfil("PROF");
    }

    private void exigirPermissaoGerenciarMembros(Conversa conversa, Usuario operadorLogado) {
        exigirUsuarioAtivo(operadorLogado);
        if ("PRIVADA".equals(conversa.getTipo())) {
            throw new SecurityException("Conversas privadas não aceitam gestão de membros.");
        }
        if (GESTORES.contains(operadorLogado.getTipoPerfil())) {
            return;
        }
        if ("PROF".equals(operadorLogado.getTipoPerfil())
                && conversa.getIdProfessorResponsavel() != null
                && operadorLogado.getIdUsuario().equals(conversa.getIdProfessorResponsavel())) {
            return;
        }
        throw new SecurityException("Você não tem permissão para gerenciar membros deste grupo.");
    }

    private void exigirAcessoConversa(Conversa conversa, Usuario usuarioLogado) {
        if (!"PRIVADA".equals(conversa.getTipo()) && GESTORES.contains(usuarioLogado.getTipoPerfil())) {
            return;
        }
        if (!chatDAO.participanteAtivo(conversa.getIdConversa(), usuarioLogado.getIdUsuario())) {
            throw new SecurityException("Você não participa desta conversa.");
        }
    }

    private Conversa buscarConversaExistente(int idConversa) {
        Conversa conversa = chatDAO.buscarPorId(idConversa);
        if (conversa == null) {
            throw new IllegalArgumentException("Conversa não encontrada.");
        }
        return conversa;
    }

    private Usuario buscarUsuarioAtivoPorPerfil(int idUsuario, String perfilEsperado) {
        Usuario usuario = usuarioDAO.buscarPorId(idUsuario);
        if (usuario == null || !"ATIVO".equals(usuario.getStatusConta()) || !perfilEsperado.equals(usuario.getTipoPerfil())) {
            throw new IllegalArgumentException("Usuário selecionado inválido.");
        }
        return usuario;
    }

    private void exigirGestorAtivo(Usuario usuarioLogado) {
        exigirUsuarioAtivo(usuarioLogado);
        if (!GESTORES.contains(usuarioLogado.getTipoPerfil())) {
            throw new SecurityException("Apenas admin ou coordenação podem criar grupos.");
        }
    }

    private void exigirAlunoAtivo(Usuario usuarioLogado) {
        exigirUsuarioAtivo(usuarioLogado);
        if (!"ALUNO".equals(usuarioLogado.getTipoPerfil())) {
            throw new SecurityException("Apenas alunos podem iniciar conversas privadas entre alunos.");
        }
    }

    private void exigirUsuarioAtivo(Usuario usuarioLogado) {
        if (!usuarioAtivoValido(usuarioLogado)) {
            throw new SecurityException("Usuário inválido ou sem permissão ativa.");
        }
    }

    private boolean usuarioAtivoValido(Usuario usuarioLogado) {
        return usuarioLogado != null
                && usuarioLogado.getIdUsuario() != null
                && usuarioLogado.getTipoPerfil() != null
                && "ATIVO".equals(usuarioLogado.getStatusConta());
    }

    private Set<Integer> idsUnicos(List<Integer> ids) {
        Set<Integer> unicos = new LinkedHashSet<>();
        if (ids != null) {
            for (Integer id : ids) {
                if (id != null) {
                    unicos.add(id);
                }
            }
        }
        return unicos;
    }

    private int exigirId(Integer id, String mensagem) {
        if (id == null) {
            throw new IllegalArgumentException(mensagem);
        }
        return id;
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
