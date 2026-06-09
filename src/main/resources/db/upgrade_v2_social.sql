USE comunicaluno_db;

DROP PROCEDURE IF EXISTS add_column_if_missing;

DELIMITER //
CREATE PROCEDURE add_column_if_missing(
  IN table_name_param VARCHAR(64),
  IN column_name_param VARCHAR(64),
  IN ddl_param TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = table_name_param
      AND column_name = column_name_param
  ) THEN
    SET @ddl = ddl_param;
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//
DELIMITER ;

CALL add_column_if_missing('usuarios', 'avatar_path',
  'ALTER TABLE usuarios ADD COLUMN avatar_path VARCHAR(500) NULL AFTER status_conta');
CALL add_column_if_missing('usuarios', 'turma',
  'ALTER TABLE usuarios ADD COLUMN turma VARCHAR(80) NULL AFTER avatar_path');
CALL add_column_if_missing('usuarios', 'curso',
  'ALTER TABLE usuarios ADD COLUMN curso VARCHAR(120) NULL AFTER turma');
CALL add_column_if_missing('comunicados', 'imagem_path',
  'ALTER TABLE comunicados ADD COLUMN imagem_path VARCHAR(500) NULL AFTER destinatario_tipo');
CALL add_column_if_missing('chamados', 'anexo_path',
  'ALTER TABLE chamados ADD COLUMN anexo_path VARCHAR(500) NULL AFTER descricao');

DROP PROCEDURE IF EXISTS add_column_if_missing;

CREATE TABLE IF NOT EXISTS comunicado_comentarios (
  id_comentario INT AUTO_INCREMENT PRIMARY KEY,
  id_comunicado INT NOT NULL,
  id_autor INT NOT NULL,
  comentario TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_comentarios_comunicado
    FOREIGN KEY (id_comunicado) REFERENCES comunicados(id_comunicado)
    ON DELETE CASCADE,
  CONSTRAINT fk_comentarios_autor
    FOREIGN KEY (id_autor) REFERENCES usuarios(id_usuario)
    ON DELETE RESTRICT,
  INDEX idx_comentarios_comunicado_created (id_comunicado, created_at)
);

CREATE TABLE IF NOT EXISTS comunicado_curtidas (
  id_comunicado INT NOT NULL,
  id_usuario INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_comunicado, id_usuario),
  CONSTRAINT fk_curtidas_comunicado
    FOREIGN KEY (id_comunicado) REFERENCES comunicados(id_comunicado)
    ON DELETE CASCADE,
  CONSTRAINT fk_curtidas_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS chamado_mensagens (
  id_mensagem INT AUTO_INCREMENT PRIMARY KEY,
  id_chamado INT NOT NULL,
  id_autor INT NOT NULL,
  mensagem TEXT NOT NULL,
  anexo_path VARCHAR(500) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_chamado_mensagens_chamado
    FOREIGN KEY (id_chamado) REFERENCES chamados(id_chamado)
    ON DELETE CASCADE,
  CONSTRAINT fk_chamado_mensagens_autor
    FOREIGN KEY (id_autor) REFERENCES usuarios(id_usuario)
    ON DELETE RESTRICT,
  INDEX idx_chamado_mensagens_created (id_chamado, created_at)
);

CREATE TABLE IF NOT EXISTS chamado_status_historico (
  id_historico INT AUTO_INCREMENT PRIMARY KEY,
  id_chamado INT NOT NULL,
  id_operador INT NOT NULL,
  status_anterior VARCHAR(30) NULL,
  status_novo VARCHAR(30) NOT NULL,
  observacao VARCHAR(255) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_chamado_historico_chamado
    FOREIGN KEY (id_chamado) REFERENCES chamados(id_chamado)
    ON DELETE CASCADE,
  CONSTRAINT fk_chamado_historico_operador
    FOREIGN KEY (id_operador) REFERENCES usuarios(id_usuario)
    ON DELETE RESTRICT,
  INDEX idx_chamado_historico_created (id_chamado, created_at)
);
