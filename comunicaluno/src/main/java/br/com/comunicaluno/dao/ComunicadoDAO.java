package br.com.comunicaluno.dao;

import br.com.comunicaluno.jdbc.ConnectionFactory;
import br.com.comunicaluno.model.Comunicado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComunicadoDAO {

    /**
     * Salva um novo aviso no banco de dados.
     */
    public void salvar(Comunicado comunicado) {
        String sql = "INSERT INTO comunicados (id_autor, titulo, mensagem, destinatario_tipo) VALUES (?, ?, ?, ?)";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, comunicado.getIdAutor());
            stmt.setString(2, comunicado.getTitulo());
            stmt.setString(3, comunicado.getMensagem());
            stmt.setString(4, comunicado.getDestinatarioTipo());

            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar comunicado: " + e.getMessage(), e);
        }
    }

    /**
     * Traz todos os comunicados que um determinado perfil (ex: ALUNO) tem permissão para ler,
     * incluindo avisos globais ('TODOS').
     * Usa JOIN para buscar o nome do autor do aviso.
     */
    public List<Comunicado> listarPorDestinatario(String tipoPerfilUsuario) {
        List<Comunicado> lista = new ArrayList<>();
        
        // Regra: O usuário vê o que é para o perfil dele + o que é para 'TODOS'
        String publicoAlvo;
        if ("ALUNO".equals(tipoPerfilUsuario)) publicoAlvo = "ALUNOS";
        else if ("PROF".equals(tipoPerfilUsuario)) publicoAlvo = "PROFESSORES";
        else if ("COORDENADOR".equals(tipoPerfilUsuario) || "ADMIN".equals(tipoPerfilUsuario)) publicoAlvo = "COORDENADORES";
        else publicoAlvo = "NENHUM";

        // JOIN blindado para trazer o nome de quem escreveu
        String sql = "SELECT c.*, u.nome as nome_autor FROM comunicados c " +
                     "INNER JOIN usuarios u ON c.id_autor = u.id_usuario " +
                     "WHERE c.destinatario_tipo = 'TODOS' OR c.destinatario_tipo = ? " +
                     "ORDER BY c.created_at DESC";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, publicoAlvo);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Comunicado c = new Comunicado();
                    c.setIdComunicado(rs.getInt("id_comunicado"));
                    c.setIdAutor(rs.getInt("id_autor"));
                    c.setNomeAutor(rs.getString("nome_autor")); // Pego no JOIN
                    c.setTitulo(rs.getString("titulo"));
                    c.setMensagem(rs.getString("mensagem"));
                    c.setDestinatarioTipo(rs.getString("destinatario_tipo"));
                    c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    
                    lista.add(c);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar comunicados: " + e.getMessage(), e);
        }

        return lista;
    }
}