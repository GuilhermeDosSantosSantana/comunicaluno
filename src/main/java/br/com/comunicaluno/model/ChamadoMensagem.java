package br.com.comunicaluno.model;

import java.time.LocalDateTime;

public class ChamadoMensagem {

    private Integer idMensagem;
    private Integer idChamado;
    private Integer idAutor;
    private String nomeAutor;
    private String perfilAutor;
    private String mensagem;
    private String anexoPath;
    private LocalDateTime createdAt;

    public Integer getIdMensagem() { return idMensagem; }
    public void setIdMensagem(Integer idMensagem) { this.idMensagem = idMensagem; }

    public Integer getIdChamado() { return idChamado; }
    public void setIdChamado(Integer idChamado) { this.idChamado = idChamado; }

    public Integer getIdAutor() { return idAutor; }
    public void setIdAutor(Integer idAutor) { this.idAutor = idAutor; }

    public String getNomeAutor() { return nomeAutor; }
    public void setNomeAutor(String nomeAutor) { this.nomeAutor = nomeAutor; }

    public String getPerfilAutor() { return perfilAutor; }
    public void setPerfilAutor(String perfilAutor) { this.perfilAutor = perfilAutor; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public String getAnexoPath() { return anexoPath; }
    public void setAnexoPath(String anexoPath) { this.anexoPath = anexoPath; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
