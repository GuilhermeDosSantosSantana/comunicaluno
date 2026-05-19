package br.com.comunicaluno.dao;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import br.com.comunicaluno.model.Usuario;

public class UsuarioDAOTest {

    @Test
    public void testarInsercaoComCriptografia() {
        // 1. O utilizador escreve a palavra-passe no ecrã (em texto limpo)
        String senhaDigitadaNoEcra = "admin123";

        // 2. BLINDAGEM ASVS: Criptografamos a palavra-passe imediatamente (Cost 12 é o padrão de mercado)
        System.out.println("A encriptar a palavra-passe...");
        String senhaCriptografada = BCrypt.hashpw(senhaDigitadaNoEcra, BCrypt.gensalt(12));
        
        System.out.println("Hash gerado: " + senhaCriptografada);

        // 3. Criamos o objeto já com o hash (nunca passamos a senha limpa para o Model)
        Usuario admin = new Usuario(
                "Guilherme Administrador", 
                "admin@comunicaluno.com.br", 
                senhaCriptografada, 
                "ADMIN"
        );
        admin.setStatusConta("ATIVO"); // Aprovamos o primeiro utilizador manualmente

        // 4. Invocamos o DAO para guardar
        UsuarioDAO dao = new UsuarioDAO();
        dao.salvar(admin);

        System.out.println("Teste concluído: Utilizador administrador guardado com segurança!");
    }
}