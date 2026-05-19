package br.com.comunicaluno.service;

import br.com.comunicaluno.dao.UsuarioDAO;
import br.com.comunicaluno.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;


public class UsuarioService {

    private UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }


    public void registarNovoUsuario(Usuario usuario) {
        
        // 1. VALIDAÇÕES BÁSICAS (Evita chamadas desnecessárias à base de dados)
        if (usuario.getNome() == null || usuario.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do utilizador não pode estar vazio.");
        }
        
        if (usuario.getEmail() == null || !usuario.getEmail().contains("@")) {
            throw new IllegalArgumentException("E-mail inválido. Por favor, insira um e-mail válido.");
        }
        
        if (usuario.getSenhaHash() == null || usuario.getSenhaHash().length() < 6) {
            throw new IllegalArgumentException("A palavra-passe deve conter pelo menos 6 caracteres.");
        }
        

        String senhaLimpa = usuario.getSenhaHash();
        String senhaCriptografada = BCrypt.hashpw(senhaLimpa, BCrypt.gensalt(12));
        

        usuario.setSenhaHash(senhaCriptografada);
        

        if (usuario.getStatusConta() == null) {
            usuario.setStatusConta("PENDENTE");
        }


        usuarioDAO.salvar(usuario);
    }


    public Usuario autenticar(String email, String senhaDigitada) {

        Usuario usuario = usuarioDAO.buscarPorEmail(email);

        if (usuario != null && BCrypt.checkpw(senhaDigitada, usuario.getSenhaHash())) {
            if (!"ATIVO".equals(usuario.getStatusConta())) {
                throw new SecurityException("Conta pendente de aprovação ou inativa.");
            }
            return usuario;
        }

        throw new SecurityException("E-mail ou palavra-passe incorretos.");
    }
}