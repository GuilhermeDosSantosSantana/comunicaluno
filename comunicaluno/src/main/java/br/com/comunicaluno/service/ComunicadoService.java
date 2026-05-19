package br.com.comunicaluno.service;

import br.com.comunicaluno.dao.ComunicadoDAO;
import br.com.comunicaluno.model.Comunicado;
import br.com.comunicaluno.model.Usuario;

import java.util.List;

public class ComunicadoService {

    private ComunicadoDAO comunicadoDAO;

    public ComunicadoService() {
        this.comunicadoDAO = new ComunicadoDAO();
    }

    /**
     * Publica um novo aviso, garantindo que as regras de negócio e segurança são cumpridas.
     */
    public void publicarAviso(Comunicado comunicado, Usuario autorLogado) {
        
        // 1. Validação de Negócio (Campos obrigatórios)
        if (comunicado.getTitulo() == null || comunicado.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("O título do aviso é obrigatório.");
        }
        if (comunicado.getMensagem() == null || comunicado.getMensagem().trim().isEmpty()) {
            throw new IllegalArgumentException("A mensagem do aviso não pode estar vazia.");
        }

        // 2. Segurança: Controlo de Acesso (RBAC)
        if ("ALUNO".equals(autorLogado.getTipoPerfil())) {
            throw new SecurityException("Violação de Acesso: Alunos não têm permissão para publicar avisos.");
        }

        // 3. Segurança: Anti-Spoofing (Garante que o autor é realmente quem está logado)
        comunicado.setIdAutor(autorLogado.getIdUsuario());

        // 4. Delega para a base de dados
        comunicadoDAO.salvar(comunicado);
    }

    /**
     * Retorna a lista de avisos que o utilizador atual tem permissão para ler.
     */
    public List<Comunicado> carregarMural(Usuario leitorLogado) {
        if (leitorLogado == null || leitorLogado.getTipoPerfil() == null) {
            throw new SecurityException("Utilizador inválido para leitura do mural.");
        }
        return comunicadoDAO.listarPorDestinatario(leitorLogado.getTipoPerfil());
    }
}