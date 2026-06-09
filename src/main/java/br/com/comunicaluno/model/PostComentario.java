package br.com.comunicaluno.model;

import java.time.LocalDateTime;

public class PostComentario {

    private Integer idComentario;
    private Integer idPost;
    private Integer idAutor;
    private String nomeAutor;
    private String perfilAutor;
    private String comentario;
    private LocalDateTime createdAt;

    public Integer getIdComentario() { return idComentario; }
    public void setIdComentario(Integer idComentario) { this.idComentario = idComentario; }

    public Integer getIdPost() { return idPost; }
    public void setIdPost(Integer idPost) { this.idPost = idPost; }

    public Integer getIdAutor() { return idAutor; }
    public void setIdAutor(Integer idAutor) { this.idAutor = idAutor; }

    public String getNomeAutor() { return nomeAutor; }
    public void setNomeAutor(String nomeAutor) { this.nomeAutor = nomeAutor; }

    public String getPerfilAutor() { return perfilAutor; }
    public void setPerfilAutor(String perfilAutor) { this.perfilAutor = perfilAutor; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
