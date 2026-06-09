package br.com.comunicaluno.dao;

import br.com.comunicaluno.jdbc.ConnectionFactory;
import br.com.comunicaluno.model.Post;
import br.com.comunicaluno.model.PostComentario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {

    public void salvar(Post post) {
        String sql = "INSERT INTO posts "
                + "(id_autor, tipo_post, titulo, texto, publico_alvo, imagem_path, anexo_path) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, post.getIdAutor());
            stmt.setString(2, post.getTipoPost());
            stmt.setString(3, post.getTitulo());
            stmt.setString(4, post.getTexto());
            stmt.setString(5, post.getPublicoAlvo());
            stmt.setString(6, post.getImagemPath());
            stmt.setString(7, post.getAnexoPath());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    post.setIdPost(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar post: " + e.getMessage(), e);
        }
    }

    public List<Post> listarFeed(String publicoAlvo, String filtroPublico, Integer idUsuarioLogado, String termoBusca) {
        List<Object> parametros = new ArrayList<>();
        StringBuilder sql = new StringBuilder(baseSelect());
        sql.append("WHERE p.deleted_at IS NULL ");
        sql.append("AND (? = 'QUALQUER' OR p.publico_alvo = 'TODOS' OR p.publico_alvo = ?) ");
        parametros.add(publicoAlvo);
        parametros.add(publicoAlvo);

        if (filtroPublico != null && !"MEU_PUBLICO".equals(filtroPublico)) {
            sql.append("AND p.publico_alvo = ? ");
            parametros.add(filtroPublico);
        }

        String busca = termoBusca == null ? null : termoBusca.trim();
        if (busca != null && !busca.isEmpty()) {
            sql.append("AND (p.titulo LIKE ? OR p.texto LIKE ? OR u.nome LIKE ?) ");
            String like = "%" + busca + "%";
            parametros.add(like);
            parametros.add(like);
            parametros.add(like);
        }

        sql.append("ORDER BY p.created_at DESC, p.id_post DESC LIMIT 100");
        return consultarPosts(sql.toString(), idUsuarioLogado, parametros);
    }

    public Post buscarPorId(int idPost, Integer idUsuarioLogado) {
        List<Object> parametros = new ArrayList<>();
        parametros.add(idPost);
        List<Post> posts = consultarPosts(baseSelect() + "WHERE p.deleted_at IS NULL AND p.id_post = ?", idUsuarioLogado, parametros);
        return posts.isEmpty() ? null : posts.get(0);
    }

    public void adicionarComentario(int idPost, int idAutor, String comentario) {
        String sql = "INSERT INTO post_comentarios (id_post, id_autor, comentario) VALUES (?, ?, ?)";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPost);
            stmt.setInt(2, idAutor);
            stmt.setString(3, comentario);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao comentar post: " + e.getMessage(), e);
        }
    }

    public List<PostComentario> listarComentarios(int idPost) {
        String sql = "SELECT pc.*, u.nome AS nome_autor, u.tipo_perfil AS perfil_autor "
                + "FROM post_comentarios pc "
                + "INNER JOIN usuarios u ON pc.id_autor = u.id_usuario "
                + "WHERE pc.id_post = ? "
                + "ORDER BY pc.created_at ASC";
        List<PostComentario> comentarios = new ArrayList<>();

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPost);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PostComentario comentario = new PostComentario();
                    comentario.setIdComentario(rs.getInt("id_comentario"));
                    comentario.setIdPost(rs.getInt("id_post"));
                    comentario.setIdAutor(rs.getInt("id_autor"));
                    comentario.setNomeAutor(rs.getString("nome_autor"));
                    comentario.setPerfilAutor(rs.getString("perfil_autor"));
                    comentario.setComentario(rs.getString("comentario"));
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        comentario.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    comentarios.add(comentario);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar comentários do post: " + e.getMessage(), e);
        }

        return comentarios;
    }

    public boolean curtidaExiste(int idPost, int idUsuario) {
        return relacionamentoExiste("post_curtidas", idPost, idUsuario);
    }

    public void adicionarCurtida(int idPost, int idUsuario) {
        executarRelacionamento("INSERT IGNORE INTO post_curtidas (id_post, id_usuario) VALUES (?, ?)", idPost, idUsuario);
    }

    public void removerCurtida(int idPost, int idUsuario) {
        executarRelacionamento("DELETE FROM post_curtidas WHERE id_post = ? AND id_usuario = ?", idPost, idUsuario);
    }

    public boolean salvoExiste(int idPost, int idUsuario) {
        return relacionamentoExiste("post_salvos", idPost, idUsuario);
    }

    public void adicionarSalvo(int idPost, int idUsuario) {
        executarRelacionamento("INSERT IGNORE INTO post_salvos (id_post, id_usuario) VALUES (?, ?)", idPost, idUsuario);
    }

    public void removerSalvo(int idPost, int idUsuario) {
        executarRelacionamento("DELETE FROM post_salvos WHERE id_post = ? AND id_usuario = ?", idPost, idUsuario);
    }

    public void arquivarPost(int idPost, int idUsuario, String motivo) {
        String sql = "UPDATE posts "
                + "SET deleted_at = CURRENT_TIMESTAMP, deleted_by = ?, delete_reason = ? "
                + "WHERE id_post = ? AND deleted_at IS NULL";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            stmt.setString(2, motivo);
            stmt.setInt(3, idPost);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao arquivar post: " + e.getMessage(), e);
        }
    }

    public int contarPostsVisiveis(String publicoAlvo) {
        String sql = "SELECT COUNT(*) FROM posts p "
                + "WHERE p.deleted_at IS NULL "
                + "AND (? = 'QUALQUER' OR p.publico_alvo = 'TODOS' OR p.publico_alvo = ?)";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, publicoAlvo);
            stmt.setString(2, publicoAlvo);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar posts: " + e.getMessage(), e);
        }
    }

    private List<Post> consultarPosts(String sql, Integer idUsuarioLogado, List<Object> parametros) {
        List<Post> posts = new ArrayList<>();

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int index = 1;
            stmt.setObject(index++, idUsuarioLogado);
            stmt.setObject(index++, idUsuarioLogado);
            stmt.setObject(index++, idUsuarioLogado);
            stmt.setObject(index++, idUsuarioLogado);
            for (Object parametro : parametros) {
                stmt.setObject(index++, parametro);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapearPost(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar feed: " + e.getMessage(), e);
        }

        return posts;
    }

    private String baseSelect() {
        return "SELECT p.*, u.nome AS nome_autor, u.tipo_perfil AS perfil_autor, u.curso AS curso_autor, "
                + "u.avatar_path AS avatar_autor_path, "
                + "(SELECT COUNT(*) FROM post_curtidas pc WHERE pc.id_post = p.id_post) AS total_curtidas, "
                + "(SELECT COUNT(*) FROM post_comentarios pcc WHERE pcc.id_post = p.id_post) AS total_comentarios, "
                + "0 AS total_reposts, "
                + "CASE WHEN ? IS NULL THEN 0 ELSE EXISTS(SELECT 1 FROM post_curtidas lu WHERE lu.id_post = p.id_post AND lu.id_usuario = ?) END AS curtido_pelo_usuario, "
                + "CASE WHEN ? IS NULL THEN 0 ELSE EXISTS(SELECT 1 FROM post_salvos ps WHERE ps.id_post = p.id_post AND ps.id_usuario = ?) END AS salvo_pelo_usuario "
                + "FROM posts p "
                + "INNER JOIN usuarios u ON p.id_autor = u.id_usuario ";
    }

    private boolean relacionamentoExiste(String tabela, int idPost, int idUsuario) {
        String sql = "SELECT 1 FROM " + tabela + " WHERE id_post = ? AND id_usuario = ?";

        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPost);
            stmt.setInt(2, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar relação do post: " + e.getMessage(), e);
        }
    }

    private void executarRelacionamento(String sql, int idPost, int idUsuario) {
        try (Connection conn = new ConnectionFactory().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPost);
            stmt.setInt(2, idUsuario);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar relação do post: " + e.getMessage(), e);
        }
    }

    private Post mapearPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setIdPost(rs.getInt("id_post"));
        post.setIdAutor(rs.getInt("id_autor"));
        post.setNomeAutor(rs.getString("nome_autor"));
        post.setPerfilAutor(rs.getString("perfil_autor"));
        post.setCursoAutor(rs.getString("curso_autor"));
        post.setAvatarAutorPath(rs.getString("avatar_autor_path"));
        post.setTipoPost(rs.getString("tipo_post"));
        post.setTitulo(rs.getString("titulo"));
        post.setTexto(rs.getString("texto"));
        post.setPublicoAlvo(rs.getString("publico_alvo"));
        post.setImagemPath(rs.getString("imagem_path"));
        post.setAnexoPath(rs.getString("anexo_path"));

        post.setTotalCurtidas(rs.getInt("total_curtidas"));
        post.setTotalComentarios(rs.getInt("total_comentarios"));
        post.setTotalReposts(rs.getInt("total_reposts"));
        post.setCurtidoPeloUsuario(rs.getBoolean("curtido_pelo_usuario"));
        post.setSalvoPeloUsuario(rs.getBoolean("salvo_pelo_usuario"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            post.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            post.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            post.setDeletedAt(deletedAt.toLocalDateTime());
        }
        int deletedBy = rs.getInt("deleted_by");
        if (!rs.wasNull()) {
            post.setDeletedBy(deletedBy);
        }
        post.setDeleteReason(rs.getString("delete_reason"));

        return post;
    }
}
