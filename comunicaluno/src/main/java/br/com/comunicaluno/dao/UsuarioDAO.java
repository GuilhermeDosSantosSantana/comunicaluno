package br.com.comunicaluno.dao;

import br.com.comunicaluno.jdbc.ConnectionFactory;
import br.com.comunicaluno.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UsuarioDAO {


    public void salvar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nome, email, senha_hash, tipo_perfil, status_conta) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenhaHash());
            stmt.setString(4, usuario.getTipoPerfil());
            stmt.setString(5, usuario.getStatusConta());

            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar utilizador na base de dados: " + e.getMessage(), e);
        }
    }


    public Usuario buscarPorEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        Usuario usuario = null;

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    usuario = new Usuario();
                    usuario.setIdUsuario(rs.getInt("id_usuario"));
                    usuario.setNome(rs.getString("nome"));
                    usuario.setEmail(rs.getString("email"));
                    usuario.setSenhaHash(rs.getString("senha_hash"));
                    usuario.setTipoPerfil(rs.getString("tipo_perfil"));
                    usuario.setStatusConta(rs.getString("status_conta"));

                    int aprovadoPor = rs.getInt("aprovado_por");
                    if (!rs.wasNull()) {
                        usuario.setAprovadoPor(aprovadoPor);
                    }

                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        usuario.setCreatedAt(createdAt.toLocalDateTime());
                    }

                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    if (updatedAt != null) {
                        usuario.setUpdatedAt(updatedAt.toLocalDateTime());
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao procurar utilizador por e-mail: " + e.getMessage(), e);
        }

        return usuario;
    }
}