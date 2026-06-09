package br.com.comunicaluno.model;

import java.time.LocalDateTime;

public class Evento {

    private Integer idEvento;
    private Integer idCriador;
    private String nomeCriador;
    private String titulo;
    private String descricao;
    private String localEvento;
    private LocalDateTime dataHora;
    private String publicoAlvo;
    private String imagemPath;
    private LocalDateTime deletedAt;
    private Integer deletedBy;
    private String deleteReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Integer getIdEvento() { return idEvento; }
    public void setIdEvento(Integer idEvento) { this.idEvento = idEvento; }

    public Integer getIdCriador() { return idCriador; }
    public void setIdCriador(Integer idCriador) { this.idCriador = idCriador; }

    public String getNomeCriador() { return nomeCriador; }
    public void setNomeCriador(String nomeCriador) { this.nomeCriador = nomeCriador; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getLocalEvento() { return localEvento; }
    public void setLocalEvento(String localEvento) { this.localEvento = localEvento; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public String getPublicoAlvo() { return publicoAlvo; }
    public void setPublicoAlvo(String publicoAlvo) { this.publicoAlvo = publicoAlvo; }

    public String getImagemPath() { return imagemPath; }
    public void setImagemPath(String imagemPath) { this.imagemPath = imagemPath; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public Integer getDeletedBy() { return deletedBy; }
    public void setDeletedBy(Integer deletedBy) { this.deletedBy = deletedBy; }

    public String getDeleteReason() { return deleteReason; }
    public void setDeleteReason(String deleteReason) { this.deleteReason = deleteReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
