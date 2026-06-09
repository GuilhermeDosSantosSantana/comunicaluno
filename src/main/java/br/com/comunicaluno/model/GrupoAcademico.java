package br.com.comunicaluno.model;

import java.util.ArrayList;
import java.util.List;

public class GrupoAcademico {

    private String nome;
    private String tipo;
    private Integer idCurso;
    private Integer idDisciplina;
    private Integer idProfessorResponsavel;
    private List<Integer> idsAlunos = new ArrayList<>();

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Integer getIdCurso() { return idCurso; }
    public void setIdCurso(Integer idCurso) { this.idCurso = idCurso; }

    public Integer getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(Integer idDisciplina) { this.idDisciplina = idDisciplina; }

    public Integer getIdProfessorResponsavel() { return idProfessorResponsavel; }
    public void setIdProfessorResponsavel(Integer idProfessorResponsavel) { this.idProfessorResponsavel = idProfessorResponsavel; }

    public List<Integer> getIdsAlunos() { return idsAlunos; }
    public void setIdsAlunos(List<Integer> idsAlunos) {
        this.idsAlunos = idsAlunos == null ? new ArrayList<>() : idsAlunos;
    }
}
