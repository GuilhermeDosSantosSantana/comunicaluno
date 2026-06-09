package br.com.comunicaluno.model;

import java.time.LocalDateTime;

public class Conversa {

    private Integer idConversa;
    private String tipo;
    private String nome;
    private Integer idCurso;
    private String nomeCurso;
    private Integer idDisciplina;
    private String nomeDisciplina;
    private Integer idProfessorResponsavel;
    private String nomeProfessorResponsavel;
    private Integer createdBy;
    private String nomeCriador;
    private LocalDateTime deletedAt;
    private Integer deletedBy;
    private String deleteReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Integer getIdConversa() { return idConversa; }
    public void setIdConversa(Integer idConversa) { this.idConversa = idConversa; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Integer getIdCurso() { return idCurso; }
    public void setIdCurso(Integer idCurso) { this.idCurso = idCurso; }

    public String getNomeCurso() { return nomeCurso; }
    public void setNomeCurso(String nomeCurso) { this.nomeCurso = nomeCurso; }

    public Integer getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(Integer idDisciplina) { this.idDisciplina = idDisciplina; }

    public String getNomeDisciplina() { return nomeDisciplina; }
    public void setNomeDisciplina(String nomeDisciplina) { this.nomeDisciplina = nomeDisciplina; }

    public Integer getIdProfessorResponsavel() { return idProfessorResponsavel; }
    public void setIdProfessorResponsavel(Integer idProfessorResponsavel) { this.idProfessorResponsavel = idProfessorResponsavel; }

    public String getNomeProfessorResponsavel() { return nomeProfessorResponsavel; }
    public void setNomeProfessorResponsavel(String nomeProfessorResponsavel) { this.nomeProfessorResponsavel = nomeProfessorResponsavel; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public String getNomeCriador() { return nomeCriador; }
    public void setNomeCriador(String nomeCriador) { this.nomeCriador = nomeCriador; }

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

    @Override
    public String toString() {
        return nome == null || nome.isBlank() ? tipo : nome;
    }
}
