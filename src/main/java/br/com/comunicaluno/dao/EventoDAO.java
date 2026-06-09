package br.com.comunicaluno.dao;

import br.com.comunicaluno.jdbc.ConnectionFactory;
import br.com.comunicaluno.model.Evento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class EventoDAO {

    public void salvar(Evento evento) {
        String sql = "INSERT INTO eventos "
                + "(id_criador, titulo, descricao, local_evento, data_hora, publico_alvo, imagem_path) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, evento.getIdCriador());
            stmt.setString(2, evento.getTitulo());
            stmt.setString(3, evento.getDescricao());
            stmt.setString(4, evento.getLocalEvento());
            stmt.setTimestamp(5, Timestamp.valueOf(evento.getDataHora()));
            stmt.setString(6, evento.getPublicoAlvo());
            stmt.setString(7, evento.getImagemPath());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    evento.setIdEvento(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar evento: " + e.getMessage(), e);
        }
    }

    public List<Evento> listarProximos(String publicoAlvo, int limite) {
        String sql = baseSelect()
                + "WHERE e.data_hora >= CURRENT_TIMESTAMP "
                + "AND e.deleted_at IS NULL "
                + "AND (? = 'QUALQUER' OR e.publico_alvo = 'TODOS' OR e.publico_alvo = ?) "
                + "ORDER BY e.data_hora ASC LIMIT ?";
        List<Evento> eventos = new ArrayList<>();

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, publicoAlvo);
            stmt.setString(2, publicoAlvo);
            stmt.setInt(3, limite);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    eventos.add(mapearEvento(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar eventos: " + e.getMessage(), e);
        }

        return eventos;
    }

    public int contarProximos(String publicoAlvo) {
        String sql = "SELECT COUNT(*) FROM eventos e "
                + "WHERE e.data_hora >= CURRENT_TIMESTAMP "
                + "AND e.deleted_at IS NULL "
                + "AND (? = 'QUALQUER' OR e.publico_alvo = 'TODOS' OR e.publico_alvo = ?)";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, publicoAlvo);
            stmt.setString(2, publicoAlvo);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar eventos: " + e.getMessage(), e);
        }
    }

    public Evento buscarPorId(int idEvento) {
        String sql = baseSelect() + "WHERE e.id_evento = ? AND e.deleted_at IS NULL";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEvento);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEvento(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar evento: " + e.getMessage(), e);
        }

        return null;
    }

    public void arquivarEvento(int idEvento, int idUsuario, String motivo) {
        String sql = "UPDATE eventos "
                + "SET deleted_at = CURRENT_TIMESTAMP, deleted_by = ?, delete_reason = ? "
                + "WHERE id_evento = ? AND deleted_at IS NULL";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            stmt.setString(2, motivo);
            stmt.setInt(3, idEvento);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao arquivar evento: " + e.getMessage(), e);
        }
    }

    private String baseSelect() {
        return "SELECT e.*, u.nome AS nome_criador "
                + "FROM eventos e "
                + "INNER JOIN usuarios u ON e.id_criador = u.id_usuario ";
    }

    private Evento mapearEvento(ResultSet rs) throws SQLException {
        Evento evento = new Evento();
        evento.setIdEvento(rs.getInt("id_evento"));
        evento.setIdCriador(rs.getInt("id_criador"));
        evento.setNomeCriador(rs.getString("nome_criador"));
        evento.setTitulo(rs.getString("titulo"));
        evento.setDescricao(rs.getString("descricao"));
        evento.setLocalEvento(rs.getString("local_evento"));
        Timestamp dataHora = rs.getTimestamp("data_hora");
        if (dataHora != null) {
            evento.setDataHora(dataHora.toLocalDateTime());
        }
        evento.setPublicoAlvo(rs.getString("publico_alvo"));
        evento.setImagemPath(rs.getString("imagem_path"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            evento.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            evento.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            evento.setDeletedAt(deletedAt.toLocalDateTime());
        }
        int deletedBy = rs.getInt("deleted_by");
        if (!rs.wasNull()) {
            evento.setDeletedBy(deletedBy);
        }
        evento.setDeleteReason(rs.getString("delete_reason"));
        return evento;
    }
}
