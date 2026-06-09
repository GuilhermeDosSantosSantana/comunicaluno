package br.com.comunicaluno.dao;

import br.com.comunicaluno.jdbc.ConnectionFactory;
import br.com.comunicaluno.model.Curso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CursoDAO {

    public List<Curso> listarAtivos() {
        String sql = "SELECT * FROM cursos WHERE ativo = TRUE ORDER BY nome ASC";
        List<Curso> cursos = new ArrayList<>();

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                cursos.add(mapearCurso(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar cursos: " + e.getMessage(), e);
        }

        return cursos;
    }

    public List<Curso> listarTodos() {
        String sql = "SELECT * FROM cursos ORDER BY ativo DESC, nome ASC";
        List<Curso> cursos = new ArrayList<>();

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                cursos.add(mapearCurso(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar todos os cursos: " + e.getMessage(), e);
        }

        return cursos;
    }

    public Curso buscarPorId(int idCurso) {
        String sql = "SELECT * FROM cursos WHERE id_curso = ? AND ativo = TRUE";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCurso);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearCurso(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar curso: " + e.getMessage(), e);
        }

        return null;
    }

    public void salvarOuAtualizar(Curso curso) {
        if (curso.getIdCurso() != null) {
            atualizar(curso);
            return;
        }

        String sql = "INSERT INTO cursos (nome, codigo, ativo) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE nome = VALUES(nome), codigo = VALUES(codigo), ativo = VALUES(ativo)";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, curso.getNome());
            stmt.setString(2, curso.getCodigo());
            stmt.setBoolean(3, curso.isAtivo());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    curso.setIdCurso(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar curso: " + e.getMessage(), e);
        }
    }

    private void atualizar(Curso curso) {
        String sql = "UPDATE cursos SET nome = ?, codigo = ?, ativo = ? WHERE id_curso = ?";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, curso.getNome());
            stmt.setString(2, curso.getCodigo());
            stmt.setBoolean(3, curso.isAtivo());
            stmt.setInt(4, curso.getIdCurso());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar curso: " + e.getMessage(), e);
        }
    }

    public void atualizarStatus(int idCurso, boolean ativo) {
        String sql = "UPDATE cursos SET ativo = ? WHERE id_curso = ?";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, ativo);
            stmt.setInt(2, idCurso);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao alterar status do curso: " + e.getMessage(), e);
        }
    }

    private Curso mapearCurso(ResultSet rs) throws SQLException {
        Curso curso = new Curso();
        curso.setIdCurso(rs.getInt("id_curso"));
        curso.setNome(rs.getString("nome"));
        curso.setCodigo(rs.getString("codigo"));
        curso.setAtivo(rs.getBoolean("ativo"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            curso.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            curso.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return curso;
    }
}
