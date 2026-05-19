package br.com.comunicaluno.model;

import java.time.LocalDateTime;

public class Comunicado {

    private Integer idComunicado;
    private Integer idAutor;
    private String nomeAutor; // Campo extra! Não existe na tabela comunicados, mas precisamos dele na tela para não mostrar apenas o "ID 1"
    private String titulo;
    private String mensagem;
    private String destinatarioTipo; // TODOS, ALUNOS, PROFESSORES, COORDENADORES
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Comunicado() {}

    // Construtor para criar um novo aviso
    public Comunicado(Integer idAutor, String titulo, String mensagem, String destinatarioTipo) {
        this.idAutor = idAutor;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.destinatarioTipo = destinatarioTipo;
    }

    // --- GETTERS E SETTERS ---
    public Integer getIdComunicado() { return idComunicado; }
    public void setIdComunicado(Integer idComunicado) { this.idComunicado = idComunicado; }

    public Integer getIdAutor() { return idAutor; }
    public void setIdAutor(Integer idAutor) { this.idAutor = idAutor; }

    public String getNomeAutor() { return nomeAutor; }
    public void setNomeAutor(String nomeAutor) { this.nomeAutor = nomeAutor; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public String getDestinatarioTipo() { return destinatarioTipo; }
    public void setDestinatarioTipo(String destinatarioTipo) { this.destinatarioTipo = destinatarioTipo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}