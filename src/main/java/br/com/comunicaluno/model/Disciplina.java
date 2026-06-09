package br.com.comunicaluno.model;

import java.time.LocalDateTime;

public class Disciplina {

    private Integer idDisciplina;
    private String nome;
    private String codigo;
    private String professorNome;
    private String capaPath;
    private int progressoPercentual;
    private LocalDateTime createdAt;

    public Integer getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(Integer idDisciplina) { this.idDisciplina = idDisciplina; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getProfessorNome() { return professorNome; }
    public void setProfessorNome(String professorNome) { this.professorNome = professorNome; }

    public String getCapaPath() { return capaPath; }
    public void setCapaPath(String capaPath) { this.capaPath = capaPath; }

    public int getProgressoPercentual() { return progressoPercentual; }
    public void setProgressoPercentual(int progressoPercentual) { this.progressoPercentual = progressoPercentual; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return nome == null ? "" : nome;
    }
}
