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

CALL add_column_if_missing('posts', 'deleted_at',
  'ALTER TABLE posts ADD COLUMN deleted_at TIMESTAMP NULL AFTER anexo_path');
CALL add_column_if_missing('posts', 'deleted_by',
  'ALTER TABLE posts ADD COLUMN deleted_by INT NULL AFTER deleted_at');
CALL add_column_if_missing('posts', 'delete_reason',
  'ALTER TABLE posts ADD COLUMN delete_reason VARCHAR(255) NULL AFTER deleted_by');

CALL add_column_if_missing('eventos', 'deleted_at',
  'ALTER TABLE eventos ADD COLUMN deleted_at TIMESTAMP NULL AFTER imagem_path');
CALL add_column_if_missing('eventos', 'deleted_by',
  'ALTER TABLE eventos ADD COLUMN deleted_by INT NULL AFTER deleted_at');
CALL add_column_if_missing('eventos', 'delete_reason',
  'ALTER TABLE eventos ADD COLUMN delete_reason VARCHAR(255) NULL AFTER deleted_by');

CALL add_column_if_missing('chamados', 'deleted_at',
  'ALTER TABLE chamados ADD COLUMN deleted_at TIMESTAMP NULL AFTER status');
CALL add_column_if_missing('chamados', 'deleted_by',
  'ALTER TABLE chamados ADD COLUMN deleted_by INT NULL AFTER deleted_at');
CALL add_column_if_missing('chamados', 'delete_reason',
  'ALTER TABLE chamados ADD COLUMN delete_reason VARCHAR(255) NULL AFTER deleted_by');

DROP PROCEDURE IF EXISTS add_column_if_missing;

UPDATE posts SET tipo_post = 'PUBLICACAO' WHERE tipo_post = 'REPOST';
ALTER TABLE posts MODIFY tipo_post ENUM('PUBLICACAO', 'AVISO', 'EVENTO') NOT NULL DEFAULT 'PUBLICACAO';
DROP TABLE IF EXISTS post_reposts;

CREATE TABLE IF NOT EXISTS cursos (
  id_curso INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(140) NOT NULL,
  codigo VARCHAR(40) NOT NULL,
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_cursos_nome UNIQUE (nome),
  CONSTRAINT uk_cursos_codigo UNIQUE (codigo)
);

CREATE TABLE IF NOT EXISTS conversas (
  id_conversa INT AUTO_INCREMENT PRIMARY KEY,
  tipo ENUM('PRIVADA', 'GRUPO_MATERIA', 'GRUPO_CURSO') NOT NULL,
  nome VARCHAR(160) NULL,
  id_curso INT NULL,
  id_disciplina INT NULL,
  id_professor_responsavel INT NULL,
  created_by INT NOT NULL,
  deleted_at TIMESTAMP NULL,
  deleted_by INT NULL,
  delete_reason VARCHAR(255) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_conversas_curso FOREIGN KEY (id_curso) REFERENCES cursos(id_curso) ON DELETE SET NULL,
  CONSTRAINT fk_conversas_disciplina FOREIGN KEY (id_disciplina) REFERENCES disciplinas(id_disciplina) ON DELETE SET NULL,
  CONSTRAINT fk_conversas_professor FOREIGN KEY (id_professor_responsavel) REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
  CONSTRAINT fk_conversas_created_by FOREIGN KEY (created_by) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
  CONSTRAINT fk_conversas_deleted_by FOREIGN KEY (deleted_by) REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
  INDEX idx_conversas_tipo (tipo, deleted_at)
);

CREATE TABLE IF NOT EXISTS conversa_participantes (
  id_conversa INT NOT NULL,
  id_usuario INT NOT NULL,
  papel ENUM('MEMBRO', 'PROF_RESPONSAVEL', 'CRIADOR') NOT NULL DEFAULT 'MEMBRO',
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  added_by INT NULL,
  removed_by INT NULL,
  joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  removed_at TIMESTAMP NULL,
  PRIMARY KEY (id_conversa, id_usuario),
  CONSTRAINT fk_conversa_participantes_conversa FOREIGN KEY (id_conversa) REFERENCES conversas(id_conversa) ON DELETE CASCADE,
  CONSTRAINT fk_conversa_participantes_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
  CONSTRAINT fk_conversa_participantes_added_by FOREIGN KEY (added_by) REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
  CONSTRAINT fk_conversa_participantes_removed_by FOREIGN KEY (removed_by) REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
  INDEX idx_conversa_participantes_usuario (id_usuario, ativo)
);

CREATE TABLE IF NOT EXISTS conversa_mensagens (
  id_mensagem INT AUTO_INCREMENT PRIMARY KEY,
  id_conversa INT NOT NULL,
  id_autor INT NOT NULL,
  mensagem TEXT NOT NULL,
  anexo_path VARCHAR(500) NULL,
  deleted_at TIMESTAMP NULL,
  deleted_by INT NULL,
  delete_reason VARCHAR(255) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_conversa_mensagens_conversa FOREIGN KEY (id_conversa) REFERENCES conversas(id_conversa) ON DELETE CASCADE,
  CONSTRAINT fk_conversa_mensagens_autor FOREIGN KEY (id_autor) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
  CONSTRAINT fk_conversa_mensagens_deleted_by FOREIGN KEY (deleted_by) REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
  INDEX idx_conversa_mensagens_created (id_conversa, created_at),
  INDEX idx_conversa_mensagens_deleted (deleted_at)
);

INSERT INTO cursos (nome, codigo)
VALUES
  ('Engenharia da Computacao', 'ENG-COMP'),
  ('Engenharia Civil', 'ENG-CIVIL'),
  ('Administracao', 'ADM'),
  ('Direito', 'DIR'),
  ('Medicina', 'MED'),
  ('Analise e Desenvolvimento de Sistemas', 'ADS')
ON DUPLICATE KEY UPDATE nome = VALUES(nome), ativo = TRUE;
