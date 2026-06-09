package br.com.comunicaluno.model;

import java.time.LocalDateTime;

public class Chamado {

    private Integer idChamado;
    private Integer idAluno;
    private String nomeAluno;
    private Integer idResponsavel;
    private String nomeResponsavel;
    private String assunto;
    private String descricao;
    private String anexoPath;
    private String status;
    private LocalDateTime deletedAt;
    private Integer deletedBy;
    private String deleteReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Chamado() {}

    public Chamado(String assunto, String descricao) {
        this.assunto = assunto;
        this.descricao = descricao;
    }

    public Integer getIdChamado() { return idChamado; }
    public void setIdChamado(Integer idChamado) { this.idChamado = idChamado; }

    public Integer getIdAluno() { return idAluno; }
    public void setIdAluno(Integer idAluno) { this.idAluno = idAluno; }

    public String getNomeAluno() { return nomeAluno; }
    public void setNomeAluno(String nomeAluno) { this.nomeAluno = nomeAluno; }

    public Integer getIdResponsavel() { return idResponsavel; }
    public void setIdResponsavel(Integer idResponsavel) { this.idResponsavel = idResponsavel; }

    public String getNomeResponsavel() { return nomeResponsavel; }
    public void setNomeResponsavel(String nomeResponsavel) { this.nomeResponsavel = nomeResponsavel; }

    public String getAssunto() { return assunto; }
    public void setAssunto(String assunto) { this.assunto = assunto; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getAnexoPath() { return anexoPath; }
    public void setAnexoPath(String anexoPath) { this.anexoPath = anexoPath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

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
