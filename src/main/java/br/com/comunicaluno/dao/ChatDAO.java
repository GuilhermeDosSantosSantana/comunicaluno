package br.com.comunicaluno.dao;

import br.com.comunicaluno.jdbc.ConnectionFactory;
import br.com.comunicaluno.model.Conversa;
import br.com.comunicaluno.model.ConversaMensagem;
import br.com.comunicaluno.model.ConversaParticipante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ChatDAO {

    public void criarConversa(Conversa conversa) {
        String sql = "INSERT INTO conversas "
                + "(tipo, nome, id_curso, id_disciplina, id_professor_responsavel, created_by) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, conversa.getTipo());
            stmt.setString(2, conversa.getNome());
            setNullableInt(stmt, 3, conversa.getIdCurso());
            setNullableInt(stmt, 4, conversa.getIdDisciplina());
            setNullableInt(stmt, 5, conversa.getIdProfessorResponsavel());
            stmt.setInt(6, conversa.getCreatedBy());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    conversa.setIdConversa(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar conversa: " + e.getMessage(), e);
        }
    }

    public Conversa buscarPorId(int idConversa) {
        String sql = baseSelect() + "WHERE c.id_conversa = ? AND c.deleted_at IS NULL";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idConversa);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearConversa(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar conversa: " + e.getMessage(), e);
        }

        return null;
    }

    public Conversa buscarPrivadaEntre(int idUsuarioA, int idUsuarioB) {
        String sql = baseSelect()
                + "WHERE c.tipo = 'PRIVADA' AND c.deleted_at IS NULL "
                + "AND EXISTS (SELECT 1 FROM conversa_participantes pa "
                + "WHERE pa.id_conversa = c.id_conversa AND pa.id_usuario = ? AND pa.ativo = TRUE) "
                + "AND EXISTS (SELECT 1 FROM conversa_participantes pb "
                + "WHERE pb.id_conversa = c.id_conversa AND pb.id_usuario = ? AND pb.ativo = TRUE) "
                + "LIMIT 1";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuarioA);
            stmt.setInt(2, idUsuarioB);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearConversa(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar conversa privada: " + e.getMessage(), e);
        }

        return null;
    }

    public List<Conversa> listarConversasParaUsuario(int idUsuario, boolean moderador) {
        String sql = baseSelect()
                + "WHERE c.deleted_at IS NULL "
                + "AND ((? = TRUE AND c.tipo <> 'PRIVADA') "
                + "OR EXISTS (SELECT 1 FROM conversa_participantes cp "
                + "WHERE cp.id_conversa = c.id_conversa AND cp.id_usuario = ? AND cp.ativo = TRUE)) "
                + "ORDER BY COALESCE((SELECT MAX(cm.created_at) FROM conversa_mensagens cm "
                + "WHERE cm.id_conversa = c.id_conversa AND cm.deleted_at IS NULL), c.updated_at) DESC, c.nome ASC";
        List<Conversa> conversas = new ArrayList<>();

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, moderador);
            stmt.setInt(2, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    conversas.add(mapearConversa(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar conversas: " + e.getMessage(), e);
        }

        return conversas;
    }

    public void adicionarParticipante(int idConversa, int idUsuario, String papel, Integer addedBy) {
        String sql = "INSERT INTO conversa_participantes (id_conversa, id_usuario, papel, ativo, added_by) "
                + "VALUES (?, ?, ?, TRUE, ?) "
                + "ON DUPLICATE KEY UPDATE papel = VALUES(papel), ativo = TRUE, added_by = VALUES(added_by), "
                + "removed_by = NULL, removed_at = NULL";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idConversa);
            stmt.setInt(2, idUsuario);
            stmt.setString(3, papel);
            setNullableInt(stmt, 4, addedBy);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao adicionar participante: " + e.getMessage(), e);
        }
    }

    public void removerParticipante(int idConversa, int idUsuario, int removedBy) {
        String sql = "UPDATE conversa_participantes "
                + "SET ativo = FALSE, removed_by = ?, removed_at = CURRENT_TIMESTAMP "
                + "WHERE id_conversa = ? AND id_usuario = ?";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, removedBy);
            stmt.setInt(2, idConversa);
            stmt.setInt(3, idUsuario);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover participante: " + e.getMessage(), e);
        }
    }

    public boolean participanteAtivo(int idConversa, int idUsuario) {
        String sql = "SELECT 1 FROM conversa_participantes "
                + "WHERE id_conversa = ? AND id_usuario = ? AND ativo = TRUE LIMIT 1";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idConversa);
            stmt.setInt(2, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar participante: " + e.getMessage(), e);
        }
    }

    public List<ConversaParticipante> listarParticipantes(int idConversa) {
        String sql = "SELECT cp.*, u.nome AS nome_usuario, u.tipo_perfil AS perfil_usuario "
                + "FROM conversa_participantes cp "
                + "INNER JOIN usuarios u ON cp.id_usuario = u.id_usuario "
                + "WHERE cp.id_conversa = ? AND cp.ativo = TRUE "
                + "ORDER BY FIELD(cp.papel, 'CRIADOR', 'PROF_RESPONSAVEL', 'MEMBRO'), u.nome ASC";
        List<ConversaParticipante> participantes = new ArrayList<>();

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idConversa);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    participantes.add(mapearParticipante(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar participantes: " + e.getMessage(), e);
        }

        return participantes;
    }

    public void enviarMensagem(ConversaMensagem mensagem) {
        String sql = "INSERT INTO conversa_mensagens (id_conversa, id_autor, mensagem, anexo_path) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, mensagem.getIdConversa());
            stmt.setInt(2, mensagem.getIdAutor());
            stmt.setString(3, mensagem.getMensagem());
            stmt.setString(4, mensagem.getAnexoPath());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    mensagem.setIdMensagem(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao enviar mensagem: " + e.getMessage(), e);
        }
    }

    public List<ConversaMensagem> listarMensagens(int idConversa) {
        String sql = "SELECT cm.*, u.nome AS nome_autor, u.tipo_perfil AS perfil_autor "
                + "FROM conversa_mensagens cm "
                + "INNER JOIN usuarios u ON cm.id_autor = u.id_usuario "
                + "WHERE cm.id_conversa = ? AND cm.deleted_at IS NULL "
                + "ORDER BY cm.created_at ASC";
        List<ConversaMensagem> mensagens = new ArrayList<>();

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idConversa);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    mensagens.add(mapearMensagem(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar mensagens: " + e.getMessage(), e);
        }

        return mensagens;
    }

    public void arquivarConversa(int idConversa, int idUsuario, String motivo) {
        String sql = "UPDATE conversas "
                + "SET deleted_at = CURRENT_TIMESTAMP, deleted_by = ?, delete_reason = ? "
                + "WHERE id_conversa = ? AND deleted_at IS NULL";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            stmt.setString(2, motivo);
            stmt.setInt(3, idConversa);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao arquivar conversa: " + e.getMessage(), e);
        }
    }

    public void arquivarMensagem(int idMensagem, int idUsuario, String motivo) {
        String sql = "UPDATE conversa_mensagens "
                + "SET deleted_at = CURRENT_TIMESTAMP, deleted_by = ?, delete_reason = ? "
                + "WHERE id_mensagem = ? AND deleted_at IS NULL";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            stmt.setString(2, motivo);
            stmt.setInt(3, idMensagem);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao arquivar mensagem: " + e.getMessage(), e);
        }
    }

    private String baseSelect() {
        return "SELECT c.*, curso.nome AS nome_curso, d.nome AS nome_disciplina, "
                + "prof.nome AS nome_professor_responsavel, criador.nome AS nome_criador "
                + "FROM conversas c "
                + "LEFT JOIN cursos curso ON c.id_curso = curso.id_curso "
                + "LEFT JOIN disciplinas d ON c.id_disciplina = d.id_disciplina "
                + "LEFT JOIN usuarios prof ON c.id_professor_responsavel = prof.id_usuario "
                + "INNER JOIN usuarios criador ON c.created_by = criador.id_usuario ";
    }

    private Conversa mapearConversa(ResultSet rs) throws SQLException {
        Conversa conversa = new Conversa();
        conversa.setIdConversa(rs.getInt("id_conversa"));
        conversa.setTipo(rs.getString("tipo"));
        conversa.setNome(rs.getString("nome"));
        int idCurso = rs.getInt("id_curso");
        if (!rs.wasNull()) {
            conversa.setIdCurso(idCurso);
        }
        conversa.setNomeCurso(rs.getString("nome_curso"));
        int idDisciplina = rs.getInt("id_disciplina");
        if (!rs.wasNull()) {
            conversa.setIdDisciplina(idDisciplina);
        }
        conversa.setNomeDisciplina(rs.getString("nome_disciplina"));
        int idProfessor = rs.getInt("id_professor_responsavel");
        if (!rs.wasNull()) {
            conversa.setIdProfessorResponsavel(idProfessor);
        }
        conversa.setNomeProfessorResponsavel(rs.getString("nome_professor_responsavel"));
        conversa.setCreatedBy(rs.getInt("created_by"));
        conversa.setNomeCriador(rs.getString("nome_criador"));
        mapearTimestampsConversa(rs, conversa);
        return conversa;
    }

    private void mapearTimestampsConversa(ResultSet rs, Conversa conversa) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            conversa.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            conversa.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            conversa.setDeletedAt(deletedAt.toLocalDateTime());
        }
        int deletedBy = rs.getInt("deleted_by");
        if (!rs.wasNull()) {
            conversa.setDeletedBy(deletedBy);
        }
        conversa.setDeleteReason(rs.getString("delete_reason"));
    }

    private ConversaParticipante mapearParticipante(ResultSet rs) throws SQLException {
        ConversaParticipante participante = new ConversaParticipante();
        participante.setIdConversa(rs.getInt("id_conversa"));
        participante.setIdUsuario(rs.getInt("id_usuario"));
        participante.setNomeUsuario(rs.getString("nome_usuario"));
        participante.setPerfilUsuario(rs.getString("perfil_usuario"));
        participante.setPapel(rs.getString("papel"));
        participante.setAtivo(rs.getBoolean("ativo"));
        int addedBy = rs.getInt("added_by");
        if (!rs.wasNull()) {
            participante.setAddedBy(addedBy);
        }
        int removedBy = rs.getInt("removed_by");
        if (!rs.wasNull()) {
            participante.setRemovedBy(removedBy);
        }
        Timestamp joinedAt = rs.getTimestamp("joined_at");
        if (joinedAt != null) {
            participante.setJoinedAt(joinedAt.toLocalDateTime());
        }
        Timestamp removedAt = rs.getTimestamp("removed_at");
        if (removedAt != null) {
            participante.setRemovedAt(removedAt.toLocalDateTime());
        }
        return participante;
    }

    private ConversaMensagem mapearMensagem(ResultSet rs) throws SQLException {
        ConversaMensagem mensagem = new ConversaMensagem();
        mensagem.setIdMensagem(rs.getInt("id_mensagem"));
        mensagem.setIdConversa(rs.getInt("id_conversa"));
        mensagem.setIdAutor(rs.getInt("id_autor"));
        mensagem.setNomeAutor(rs.getString("nome_autor"));
        mensagem.setPerfilAutor(rs.getString("perfil_autor"));
        mensagem.setMensagem(rs.getString("mensagem"));
        mensagem.setAnexoPath(rs.getString("anexo_path"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            mensagem.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            mensagem.setDeletedAt(deletedAt.toLocalDateTime());
        }
        int deletedBy = rs.getInt("deleted_by");
        if (!rs.wasNull()) {
            mensagem.setDeletedBy(deletedBy);
        }
        mensagem.setDeleteReason(rs.getString("delete_reason"));
        return mensagem;
    }

    private void setNullableInt(PreparedStatement stmt, int index, Integer valor) throws SQLException {
        if (valor == null) {
            stmt.setNull(index, java.sql.Types.INTEGER);
        } else {
            stmt.setInt(index, valor);
        }
    }
}
