package br.com.comunicaluno.dao;

import br.com.comunicaluno.jdbc.ConnectionFactory;
import br.com.comunicaluno.model.Disciplina;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DisciplinaDAO {

    public List<Disciplina> listarParaUsuario(int idUsuario) {
        String sql = "SELECT d.*, COALESCE(ud.progresso_percentual, 0) AS progresso_percentual "
                + "FROM disciplinas d "
                + "LEFT JOIN usuario_disciplinas ud ON ud.id_disciplina = d.id_disciplina AND ud.id_usuario = ? "
                + "ORDER BY d.nome ASC";
        List<Disciplina> disciplinas = new ArrayList<>();

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    disciplinas.add(mapearDisciplina(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar disciplinas: " + e.getMessage(), e);
        }

        return disciplinas;
    }

    public int contarDisponiveis() {
        String sql = "SELECT COUNT(*) FROM disciplinas";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar disciplinas: " + e.getMessage(), e);
        }
    }

    public List<Disciplina> listarTodas() {
        String sql = "SELECT d.*, 0 AS progresso_percentual FROM disciplinas d ORDER BY d.nome ASC";
        List<Disciplina> disciplinas = new ArrayList<>();

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                disciplinas.add(mapearDisciplina(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar catálogo de disciplinas: " + e.getMessage(), e);
        }

        return disciplinas;
    }

    public Disciplina buscarPorId(int idDisciplina) {
        String sql = "SELECT d.*, 0 AS progresso_percentual FROM disciplinas d WHERE d.id_disciplina = ?";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idDisciplina);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearDisciplina(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar disciplina: " + e.getMessage(), e);
        }

        return null;
    }

    public void salvarOuAtualizar(Disciplina disciplina) {
        String sql = "INSERT INTO disciplinas (nome, codigo, professor_nome, capa_path) VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE nome = VALUES(nome), professor_nome = VALUES(professor_nome), capa_path = VALUES(capa_path)";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, disciplina.getNome());
            stmt.setString(2, disciplina.getCodigo());
            stmt.setString(3, disciplina.getProfessorNome());
            stmt.setString(4, disciplina.getCapaPath());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar disciplina: " + e.getMessage(), e);
        }
    }

    private Disciplina mapearDisciplina(ResultSet rs) throws SQLException {
        Disciplina disciplina = new Disciplina();
        disciplina.setIdDisciplina(rs.getInt("id_disciplina"));
        disciplina.setNome(rs.getString("nome"));
        disciplina.setCodigo(rs.getString("codigo"));
        disciplina.setProfessorNome(rs.getString("professor_nome"));
        disciplina.setCapaPath(rs.getString("capa_path"));
        disciplina.setProgressoPercentual(rs.getInt("progresso_percentual"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            disciplina.setCreatedAt(createdAt.toLocalDateTime());
        }
        return disciplina;
    }
}
