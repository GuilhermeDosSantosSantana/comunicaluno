package br.com.comunicaluno.dao;

import br.com.comunicaluno.jdbc.ConnectionFactory;
import br.com.comunicaluno.model.Chamado;
import br.com.comunicaluno.model.ChamadoMensagem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ChamadoDAO {

    public void salvar(Chamado chamado) {
        String sql = "INSERT INTO chamados (id_aluno, assunto, descricao, anexo_path, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, chamado.getIdAluno());
            stmt.setString(2, chamado.getAssunto());
            stmt.setString(3, chamado.getDescricao());
            stmt.setString(4, chamado.getAnexoPath());
            stmt.setString(5, chamado.getStatus());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    chamado.setIdChamado(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar chamado: " + e.getMessage(), e);
        }
    }

    public List<Chamado> listarPorAluno(int idAluno) {
        String sql = baseSelect()
                + "WHERE c.id_aluno = ? AND c.deleted_at IS NULL "
                + "ORDER BY FIELD(c.status, 'ABERTO', 'EM_ANALISE', 'RESOLVIDO', 'FECHADO'), c.created_at DESC";
        List<Chamado> chamados = new ArrayList<>();

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idAluno);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    chamados.add(mapearChamado(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar chamados do aluno: " + e.getMessage(), e);
        }

        return chamados;
    }

    public List<Chamado> listarFilaGeral() {
        String sql = baseSelect()
                + "WHERE c.deleted_at IS NULL "
                + "ORDER BY FIELD(c.status, 'ABERTO', 'EM_ANALISE', 'RESOLVIDO', 'FECHADO'), c.created_at DESC";
        List<Chamado> chamados = new ArrayList<>();

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                chamados.add(mapearChamado(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar fila geral de chamados: " + e.getMessage(), e);
        }

        return chamados;
    }

    public void assumirChamado(int idChamado, int idResponsavel) {
        String sql = "UPDATE chamados SET id_responsavel = ?, status = 'EM_ANALISE' WHERE id_chamado = ?";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idResponsavel);
            stmt.setInt(2, idChamado);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao assumir chamado: " + e.getMessage(), e);
        }
    }

    public void atualizarStatus(int idChamado, String status) {
        String sql = "UPDATE chamados SET status = ? WHERE id_chamado = ?";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, idChamado);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status do chamado: " + e.getMessage(), e);
        }
    }

    public Chamado buscarPorId(int idChamado) {
        String sql = baseSelect() + "WHERE c.id_chamado = ? AND c.deleted_at IS NULL";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idChamado);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearChamado(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar chamado: " + e.getMessage(), e);
        }

        return null;
    }

    public void arquivarChamado(int idChamado, int idUsuario, String motivo) {
        String sql = "UPDATE chamados "
                + "SET deleted_at = CURRENT_TIMESTAMP, deleted_by = ?, delete_reason = ? "
                + "WHERE id_chamado = ? AND deleted_at IS NULL";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            stmt.setString(2, motivo);
            stmt.setInt(3, idChamado);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao arquivar chamado: " + e.getMessage(), e);
        }
    }

    public void adicionarMensagem(ChamadoMensagem mensagem) {
        String sql = "INSERT INTO chamado_mensagens (id_chamado, id_autor, mensagem, anexo_path) VALUES (?, ?, ?, ?)";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mensagem.getIdChamado());
            stmt.setInt(2, mensagem.getIdAutor());
            stmt.setString(3, mensagem.getMensagem());
            stmt.setString(4, mensagem.getAnexoPath());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao enviar mensagem do chamado: " + e.getMessage(), e);
        }
    }

    public List<ChamadoMensagem> listarMensagens(int idChamado) {
        String sql = "SELECT m.*, u.nome AS nome_autor, u.tipo_perfil AS perfil_autor "
                + "FROM chamado_mensagens m "
                + "INNER JOIN usuarios u ON m.id_autor = u.id_usuario "
                + "WHERE m.id_chamado = ? "
                + "ORDER BY m.created_at ASC";
        List<ChamadoMensagem> mensagens = new ArrayList<>();

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idChamado);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ChamadoMensagem mensagem = new ChamadoMensagem();
                    mensagem.setIdMensagem(rs.getInt("id_mensagem"));
                    mensagem.setIdChamado(rs.getInt("id_chamado"));
                    mensagem.setIdAutor(rs.getInt("id_autor"));
                    mensagem.setNomeAutor(rs.getString("nome_autor"));
                    mensagem.setPerfilAutor(rs.getString("perfil_autor"));
                    mensagem.setMensagem(rs.getString("mensagem"));
                    mensagem.setAnexoPath(rs.getString("anexo_path"));
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        mensagem.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    mensagens.add(mensagem);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar mensagens do chamado: " + e.getMessage(), e);
        }

        return mensagens;
    }

    public void registrarHistoricoStatus(int idChamado, Integer idOperador, String statusAnterior, String statusNovo, String observacao) {
        String sql = "INSERT INTO chamado_status_historico "
                + "(id_chamado, id_operador, status_anterior, status_novo, observacao) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idChamado);
            stmt.setInt(2, idOperador);
            stmt.setString(3, statusAnterior);
            stmt.setString(4, statusNovo);
            stmt.setString(5, observacao);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao registrar histórico do chamado: " + e.getMessage(), e);
        }
    }

    public int contarPorStatus(String status) {
        String sql = "SELECT COUNT(*) FROM chamados WHERE status = ? AND deleted_at IS NULL";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar chamados por status: " + e.getMessage(), e);
        }

        return 0;
    }

    private String baseSelect() {
        return "SELECT c.*, aluno.nome AS nome_aluno, resp.nome AS nome_responsavel "
                + "FROM chamados c "
                + "INNER JOIN usuarios aluno ON c.id_aluno = aluno.id_usuario "
                + "LEFT JOIN usuarios resp ON c.id_responsavel = resp.id_usuario ";
    }

    private Chamado mapearChamado(ResultSet rs) throws SQLException {
        Chamado chamado = new Chamado();
        chamado.setIdChamado(rs.getInt("id_chamado"));
        chamado.setIdAluno(rs.getInt("id_aluno"));
        chamado.setNomeAluno(rs.getString("nome_aluno"));

        int idResponsavel = rs.getInt("id_responsavel");
        if (!rs.wasNull()) {
            chamado.setIdResponsavel(idResponsavel);
        }
        chamado.setNomeResponsavel(rs.getString("nome_responsavel"));
        chamado.setAssunto(rs.getString("assunto"));
        chamado.setDescricao(rs.getString("descricao"));
        chamado.setAnexoPath(rs.getString("anexo_path"));
        chamado.setStatus(rs.getString("status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            chamado.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            chamado.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            chamado.setDeletedAt(deletedAt.toLocalDateTime());
        }
        int deletedBy = rs.getInt("deleted_by");
        if (!rs.wasNull()) {
            chamado.setDeletedBy(deletedBy);
        }
        chamado.setDeleteReason(rs.getString("delete_reason"));

        return chamado;
    }
}
