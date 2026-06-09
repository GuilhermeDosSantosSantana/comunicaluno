package br.com.comunicaluno.dao;

import br.com.comunicaluno.jdbc.ConnectionFactory;
import br.com.comunicaluno.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {


    public void salvar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nome, email, senha_hash, tipo_perfil, status_conta, avatar_path, turma, curso) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenhaHash());
            stmt.setString(4, usuario.getTipoPerfil());
            stmt.setString(5, usuario.getStatusConta());
            stmt.setString(6, usuario.getAvatarPath());
            stmt.setString(7, usuario.getTurma());
            stmt.setString(8, usuario.getCurso());

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
                    usuario = mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao procurar utilizador por e-mail: " + e.getMessage(), e);
        }

        return usuario;
    }

    public Usuario buscarPorId(int idUsuario) {
        String sql = "SELECT * FROM usuarios WHERE id_usuario = ?";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao procurar utilizador por id: " + e.getMessage(), e);
        }

        return null;
    }

    public boolean emailExiste(String email) {
        String sql = "SELECT 1 FROM usuarios WHERE email = ? LIMIT 1";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar e-mail existente: " + e.getMessage(), e);
        }
    }

    public List<Usuario> listarPorStatus(String statusConta) {
        String sql = "SELECT * FROM usuarios WHERE status_conta = ? ORDER BY created_at ASC, nome ASC";
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, statusConta);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapearUsuario(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar utilizadores por status: " + e.getMessage(), e);
        }

        return usuarios;
    }

    public List<Usuario> listarAtivosPorPerfil(String tipoPerfil) {
        String sql = "SELECT * FROM usuarios "
                + "WHERE status_conta = 'ATIVO' AND tipo_perfil = ? "
                + "ORDER BY nome ASC";
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipoPerfil);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapearUsuario(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar utilizadores ativos por perfil: " + e.getMessage(), e);
        }

        return usuarios;
    }

    public void atualizarStatusConta(int idUsuario, String novoStatus, Integer aprovadoPor) {
        String sql = "UPDATE usuarios SET status_conta = ?, aprovado_por = ? WHERE id_usuario = ?";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novoStatus);
            if (aprovadoPor == null) {
                stmt.setNull(2, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(2, aprovadoPor);
            }
            stmt.setInt(3, idUsuario);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status do utilizador: " + e.getMessage(), e);
        }
    }

    public void atualizarPerfil(Usuario usuario) {
        String sql = "UPDATE usuarios SET nome = ?, avatar_path = ?, turma = ?, curso = ? WHERE id_usuario = ?";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getAvatarPath());
            stmt.setString(3, usuario.getTurma());
            stmt.setString(4, usuario.getCurso());
            stmt.setInt(5, usuario.getIdUsuario());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar perfil do utilizador: " + e.getMessage(), e);
        }
    }

    public int contarPorStatus(String statusConta) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE status_conta = ?";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, statusConta);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar utilizadores por status: " + e.getMessage(), e);
        }

        return 0;
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNome(rs.getString("nome"));
        usuario.setEmail(rs.getString("email"));
        usuario.setSenhaHash(rs.getString("senha_hash"));
        usuario.setTipoPerfil(rs.getString("tipo_perfil"));
        usuario.setStatusConta(rs.getString("status_conta"));
        usuario.setAvatarPath(rs.getString("avatar_path"));
        usuario.setTurma(rs.getString("turma"));
        usuario.setCurso(rs.getString("curso"));

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

        return usuario;
    }
}
