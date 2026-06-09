package br.com.comunicaluno.dao;

import br.com.comunicaluno.jdbc.ConnectionFactory;
import br.com.comunicaluno.model.Notificacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class NotificacaoDAO {

    public void salvar(Notificacao notificacao) {
        String sql = "INSERT INTO notificacoes (id_usuario, titulo, mensagem, tipo, destino, lida) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (notificacao.getIdUsuario() == null) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, notificacao.getIdUsuario());
            }
            stmt.setString(2, notificacao.getTitulo());
            stmt.setString(3, notificacao.getMensagem());
            stmt.setString(4, notificacao.getTipo());
            stmt.setString(5, notificacao.getDestino());
            stmt.setBoolean(6, notificacao.isLida());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar notificação: " + e.getMessage(), e);
        }
    }

    public List<Notificacao> listarRecentes(int idUsuario, int limite) {
        String sql = "SELECT * FROM notificacoes "
                + "WHERE id_usuario = ? OR id_usuario IS NULL "
                + "ORDER BY created_at DESC LIMIT ?";
        List<Notificacao> notificacoes = new ArrayList<>();

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            stmt.setInt(2, limite);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notificacoes.add(mapearNotificacao(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar notificações: " + e.getMessage(), e);
        }

        return notificacoes;
    }

    public int contarNaoLidas(int idUsuario) {
        String sql = "SELECT COUNT(*) FROM notificacoes WHERE (id_usuario = ? OR id_usuario IS NULL) AND lida = FALSE";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar notificações: " + e.getMessage(), e);
        }
    }

    public void marcarTodasComoLidas(int idUsuario) {
        String sql = "UPDATE notificacoes SET lida = TRUE WHERE id_usuario = ? OR id_usuario IS NULL";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao marcar notificações como lidas: " + e.getMessage(), e);
        }
    }

    private Notificacao mapearNotificacao(ResultSet rs) throws SQLException {
        Notificacao notificacao = new Notificacao();
        notificacao.setIdNotificacao(rs.getInt("id_notificacao"));

        int idUsuario = rs.getInt("id_usuario");
        if (!rs.wasNull()) {
            notificacao.setIdUsuario(idUsuario);
        }

        notificacao.setTitulo(rs.getString("titulo"));
        notificacao.setMensagem(rs.getString("mensagem"));
        notificacao.setTipo(rs.getString("tipo"));
        notificacao.setDestino(rs.getString("destino"));
        notificacao.setLida(rs.getBoolean("lida"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            notificacao.setCreatedAt(createdAt.toLocalDateTime());
        }

        return notificacao;
    }
}
