package br.com.comunicaluno.service;

import br.com.comunicaluno.dao.UsuarioDAO;
import br.com.comunicaluno.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;


public class UsuarioService {

    private static final int BCRYPT_COST = 12;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TEM_LETRA = Pattern.compile(".*[A-Za-z].*");
    private static final Pattern TEM_NUMERO = Pattern.compile(".*[0-9].*");
    private static final Set<String> PERFIS_CADASTRO_PUBLICO = Set.of("ALUNO", "PROF", "COORDENADOR");

    private final UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public void registarNovoUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Dados do usuário obrigatórios.");
        }

        if (usuario.getNome() == null || usuario.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do usuário não pode estar vazio.");
        }

        String emailNormalizado = normalizarEmail(usuario.getEmail());
        validarEmailCadastro(emailNormalizado);
        validarSenhaCadastro(usuario.getSenhaHash());

        if (!PERFIS_CADASTRO_PUBLICO.contains(usuario.getTipoPerfil())) {
            throw new IllegalArgumentException("Perfil inválido para cadastro público.");
        }

        if (usuarioDAO.emailExiste(emailNormalizado)) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado.");
        }

        String senhaLimpa = usuario.getSenhaHash();
        String senhaCriptografada = BCrypt.hashpw(senhaLimpa, BCrypt.gensalt(BCRYPT_COST));

        usuario.setNome(usuario.getNome().trim());
        usuario.setEmail(emailNormalizado);
        usuario.setSenhaHash(senhaCriptografada);
        usuario.setStatusConta("PENDENTE");

        usuarioDAO.salvar(usuario);
    }


    public Usuario autenticar(String email, String senhaDigitada) {
        String emailNormalizado = normalizarEmail(email);
        if (emailNormalizado == null || senhaDigitada == null) {
            throw new SecurityException("E-mail ou senha incorretos.");
        }

        Usuario usuario = usuarioDAO.buscarPorEmail(emailNormalizado);

        if (usuario != null && BCrypt.checkpw(senhaDigitada, usuario.getSenhaHash())) {
            if (!"ATIVO".equals(usuario.getStatusConta())) {
                throw new SecurityException("Conta pendente de aprovação ou inativa.");
            }
            return usuario;
        }

        throw new SecurityException("E-mail ou senha incorretos.");
    }

    public List<Usuario> listarContasPendentes(Usuario operadorLogado) {
        exigirGestorContas(operadorLogado);
        return usuarioDAO.listarPorStatus("PENDENTE");
    }

    public void aprovarUsuario(int idUsuario, Usuario operadorLogado) {
        exigirGestorContas(operadorLogado);
        usuarioDAO.atualizarStatusConta(idUsuario, "ATIVO", operadorLogado.getIdUsuario());
    }

    public void inativarUsuario(int idUsuario, Usuario operadorLogado) {
        exigirGestorContas(operadorLogado);
        usuarioDAO.atualizarStatusConta(idUsuario, "INATIVO", operadorLogado.getIdUsuario());
    }

    public void atualizarPerfil(Usuario usuarioAtualizado, Usuario usuarioLogado) {
        exigirUsuarioAtivo(usuarioLogado);
        if (usuarioAtualizado == null || usuarioAtualizado.getIdUsuario() == null) {
            throw new IllegalArgumentException("Dados do perfil obrigatórios.");
        }
        if (!usuarioAtualizado.getIdUsuario().equals(usuarioLogado.getIdUsuario())) {
            throw new SecurityException("Só é permitido atualizar o próprio perfil.");
        }
        if (usuarioAtualizado.getNome() == null || usuarioAtualizado.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome não pode estar vazio.");
        }

        usuarioAtualizado.setNome(usuarioAtualizado.getNome().trim());
        usuarioAtualizado.setAvatarPath(normalizarTextoOpcional(usuarioAtualizado.getAvatarPath()));
        usuarioAtualizado.setTurma(normalizarTextoOpcional(usuarioAtualizado.getTurma()));
        usuarioAtualizado.setCurso(normalizarTextoOpcional(usuarioAtualizado.getCurso()));

        usuarioDAO.atualizarPerfil(usuarioAtualizado);
        usuarioLogado.setNome(usuarioAtualizado.getNome());
        usuarioLogado.setAvatarPath(usuarioAtualizado.getAvatarPath());
        usuarioLogado.setTurma(usuarioAtualizado.getTurma());
        usuarioLogado.setCurso(usuarioAtualizado.getCurso());
    }

    public int contarContasPendentes(Usuario operadorLogado) {
        exigirGestorContas(operadorLogado);
        return usuarioDAO.contarPorStatus("PENDENTE");
    }

    public boolean podeAdministrarContas(Usuario usuario) {
        return usuario != null
                && ("ADMIN".equals(usuario.getTipoPerfil()) || "COORDENADOR".equals(usuario.getTipoPerfil()));
    }

    private void exigirGestorContas(Usuario operadorLogado) {
        if (!podeAdministrarContas(operadorLogado) || operadorLogado.getIdUsuario() == null) {
            throw new SecurityException("Acesso negado: apenas administradores e coordenadores podem gerir contas.");
        }
        if (!"ATIVO".equals(operadorLogado.getStatusConta())) {
            throw new SecurityException("Conta sem permissão ativa para gerir contas.");
        }
    }

    private void exigirUsuarioAtivo(Usuario usuarioLogado) {
        if (usuarioLogado == null || usuarioLogado.getIdUsuario() == null) {
            throw new SecurityException("Usuário inválido.");
        }
        if (!"ATIVO".equals(usuarioLogado.getStatusConta())) {
            throw new SecurityException("Conta sem permissão ativa.");
        }
    }

    public boolean emailValido(String email) {
        String normalizado = normalizarEmail(email);
        return normalizado != null && EMAIL_PATTERN.matcher(normalizado).matches();
    }

    public boolean senhaCadastroValida(String senha) {
        return senha != null
                && senha.length() >= 6
                && TEM_LETRA.matcher(senha).matches()
                && TEM_NUMERO.matcher(senha).matches();
    }

    private void validarEmailCadastro(String emailNormalizado) {
        if (emailNormalizado == null || !EMAIL_PATTERN.matcher(emailNormalizado).matches()) {
            throw new IllegalArgumentException("E-mail inválido. Use um formato como nome@dominio.com.");
        }
    }

    private void validarSenhaCadastro(String senha) {
        if (!senhaCadastroValida(senha)) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 6 caracteres, com letras e números.");
        }
    }

    private String normalizarEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalizado = email.trim().toLowerCase();
        return normalizado.isEmpty() ? null : normalizado;
    }

    private String normalizarTextoOpcional(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return valor.trim();
    }
}
