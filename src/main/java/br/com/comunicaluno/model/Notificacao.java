package br.com.comunicaluno.model;

import java.time.LocalDateTime;

public class Notificacao {

    private Integer idNotificacao;
    private Integer idUsuario;
    private String titulo;
    private String mensagem;
    private String tipo;
    private String destino;
    private boolean lida;
    private LocalDateTime createdAt;

    public Integer getIdNotificacao() { return idNotificacao; }
    public void setIdNotificacao(Integer idNotificacao) { this.idNotificacao = idNotificacao; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public boolean isLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
