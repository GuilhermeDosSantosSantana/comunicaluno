package br.com.comunicaluno.model;

import java.time.LocalDateTime;

public class Post {

    private Integer idPost;
    private Integer idAutor;
    private String nomeAutor;
    private String perfilAutor;
    private String cursoAutor;
    private String avatarAutorPath;
    private String tipoPost;
    private String titulo;
    private String texto;
    private String publicoAlvo;
    private String imagemPath;
    private String anexoPath;
    private Integer repostDeId;
    private LocalDateTime deletedAt;
    private Integer deletedBy;
    private String deleteReason;
    private int totalCurtidas;
    private int totalComentarios;
    private int totalReposts;
    private boolean curtidoPeloUsuario;
    private boolean salvoPeloUsuario;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Post() {
    }

    public Post(String texto, String publicoAlvo) {
        this.texto = texto;
        this.publicoAlvo = publicoAlvo;
        this.tipoPost = "PUBLICACAO";
    }

    public Integer getIdPost() { return idPost; }
    public void setIdPost(Integer idPost) { this.idPost = idPost; }

    public Integer getIdAutor() { return idAutor; }
    public void setIdAutor(Integer idAutor) { this.idAutor = idAutor; }

    public String getNomeAutor() { return nomeAutor; }
    public void setNomeAutor(String nomeAutor) { this.nomeAutor = nomeAutor; }

    public String getPerfilAutor() { return perfilAutor; }
    public void setPerfilAutor(String perfilAutor) { this.perfilAutor = perfilAutor; }

    public String getCursoAutor() { return cursoAutor; }
    public void setCursoAutor(String cursoAutor) { this.cursoAutor = cursoAutor; }

    public String getAvatarAutorPath() { return avatarAutorPath; }
    public void setAvatarAutorPath(String avatarAutorPath) { this.avatarAutorPath = avatarAutorPath; }

    public String getTipoPost() { return tipoPost; }
    public void setTipoPost(String tipoPost) { this.tipoPost = tipoPost; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getPublicoAlvo() { return publicoAlvo; }
    public void setPublicoAlvo(String publicoAlvo) { this.publicoAlvo = publicoAlvo; }

    public String getImagemPath() { return imagemPath; }
    public void setImagemPath(String imagemPath) { this.imagemPath = imagemPath; }

    public String getAnexoPath() { return anexoPath; }
    public void setAnexoPath(String anexoPath) { this.anexoPath = anexoPath; }

    public Integer getRepostDeId() { return repostDeId; }
    public void setRepostDeId(Integer repostDeId) { this.repostDeId = repostDeId; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public Integer getDeletedBy() { return deletedBy; }
    public void setDeletedBy(Integer deletedBy) { this.deletedBy = deletedBy; }

    public String getDeleteReason() { return deleteReason; }
    public void setDeleteReason(String deleteReason) { this.deleteReason = deleteReason; }

    public int getTotalCurtidas() { return totalCurtidas; }
    public void setTotalCurtidas(int totalCurtidas) { this.totalCurtidas = totalCurtidas; }

    public int getTotalComentarios() { return totalComentarios; }
    public void setTotalComentarios(int totalComentarios) { this.totalComentarios = totalComentarios; }

    public int getTotalReposts() { return totalReposts; }
    public void setTotalReposts(int totalReposts) { this.totalReposts = totalReposts; }

    public boolean isCurtidoPeloUsuario() { return curtidoPeloUsuario; }
    public void setCurtidoPeloUsuario(boolean curtidoPeloUsuario) { this.curtidoPeloUsuario = curtidoPeloUsuario; }

    public boolean isSalvoPeloUsuario() { return salvoPeloUsuario; }
    public void setSalvoPeloUsuario(boolean salvoPeloUsuario) { this.salvoPeloUsuario = salvoPeloUsuario; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
