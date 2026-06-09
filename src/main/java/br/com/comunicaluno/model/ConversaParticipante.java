package br.com.comunicaluno.model;

import java.time.LocalDateTime;

public class ConversaParticipante {

    private Integer idConversa;
    private Integer idUsuario;
    private String nomeUsuario;
    private String perfilUsuario;
    private String papel;
    private boolean ativo = true;
    private Integer addedBy;
    private Integer removedBy;
    private LocalDateTime joinedAt;
    private LocalDateTime removedAt;

    public Integer getIdConversa() { return idConversa; }
    public void setIdConversa(Integer idConversa) { this.idConversa = idConversa; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }

    public String getPerfilUsuario() { return perfilUsuario; }
    public void setPerfilUsuario(String perfilUsuario) { this.perfilUsuario = perfilUsuario; }

    public String getPapel() { return papel; }
    public void setPapel(String papel) { this.papel = papel; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public Integer getAddedBy() { return addedBy; }
    public void setAddedBy(Integer addedBy) { this.addedBy = addedBy; }

    public Integer getRemovedBy() { return removedBy; }
    public void setRemovedBy(Integer removedBy) { this.removedBy = removedBy; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public LocalDateTime getRemovedAt() { return removedAt; }
    public void setRemovedAt(LocalDateTime removedAt) { this.removedAt = removedAt; }

    @Override
    public String toString() {
        return nomeUsuario == null ? "" : nomeUsuario + " - " + perfilUsuario;
    }
}
