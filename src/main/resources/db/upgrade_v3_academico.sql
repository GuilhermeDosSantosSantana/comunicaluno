USE comunicaluno_db;

CREATE TABLE IF NOT EXISTS posts (
  id_post INT AUTO_INCREMENT PRIMARY KEY,
  id_autor INT NOT NULL,
  tipo_post ENUM('PUBLICACAO', 'AVISO', 'EVENTO', 'REPOST') NOT NULL DEFAULT 'PUBLICACAO',
  titulo VARCHAR(160) NULL,
  texto TEXT NOT NULL,
  publico_alvo ENUM('TODOS', 'ALUNOS', 'PROFESSORES', 'COORDENADORES') NOT NULL DEFAULT 'TODOS',
  imagem_path VARCHAR(500) NULL,
  anexo_path VARCHAR(500) NULL,
  repost_de_id INT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_posts_autor FOREIGN KEY (id_autor) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
  CONSTRAINT fk_posts_repost_origem FOREIGN KEY (repost_de_id) REFERENCES posts(id_post) ON DELETE SET NULL,
  INDEX idx_posts_publico_created (publico_alvo, created_at),
  INDEX idx_posts_autor_created (id_autor, created_at)
);

CREATE TABLE IF NOT EXISTS post_comentarios (
  id_comentario INT AUTO_INCREMENT PRIMARY KEY,
  id_post INT NOT NULL,
  id_autor INT NOT NULL,
  comentario TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_post_comentarios_post FOREIGN KEY (id_post) REFERENCES posts(id_post) ON DELETE CASCADE,
  CONSTRAINT fk_post_comentarios_autor FOREIGN KEY (id_autor) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
  INDEX idx_post_comentarios_created (id_post, created_at)
);

CREATE TABLE IF NOT EXISTS post_curtidas (
  id_post INT NOT NULL,
  id_usuario INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_post, id_usuario),
  CONSTRAINT fk_post_curtidas_post FOREIGN KEY (id_post) REFERENCES posts(id_post) ON DELETE CASCADE,
  CONSTRAINT fk_post_curtidas_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS post_salvos (
  id_post INT NOT NULL,
  id_usuario INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_post, id_usuario),
  CONSTRAINT fk_post_salvos_post FOREIGN KEY (id_post) REFERENCES posts(id_post) ON DELETE CASCADE,
  CONSTRAINT fk_post_salvos_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS post_reposts (
  id_post INT NOT NULL,
  id_usuario INT NOT NULL,
  comentario VARCHAR(255) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_post, id_usuario),
  CONSTRAINT fk_post_reposts_post FOREIGN KEY (id_post) REFERENCES posts(id_post) ON DELETE CASCADE,
  CONSTRAINT fk_post_reposts_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS eventos (
  id_evento INT AUTO_INCREMENT PRIMARY KEY,
  id_criador INT NOT NULL,
  titulo VARCHAR(160) NOT NULL,
  descricao TEXT NOT NULL,
  local_evento VARCHAR(160) NULL,
  data_hora DATETIME NOT NULL,
  publico_alvo ENUM('TODOS', 'ALUNOS', 'PROFESSORES', 'COORDENADORES') NOT NULL DEFAULT 'TODOS',
  imagem_path VARCHAR(500) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_eventos_criador FOREIGN KEY (id_criador) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
  INDEX idx_eventos_publico_data (publico_alvo, data_hora)
);

CREATE TABLE IF NOT EXISTS disciplinas (
  id_disciplina INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(140) NOT NULL,
  codigo VARCHAR(40) NOT NULL,
  professor_nome VARCHAR(120) NULL,
  capa_path VARCHAR(500) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_disciplinas_codigo UNIQUE (codigo)
);

CREATE TABLE IF NOT EXISTS usuario_disciplinas (
  id_usuario INT NOT NULL,
  id_disciplina INT NOT NULL,
  progresso_percentual INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_usuario, id_disciplina),
  CONSTRAINT fk_usuario_disciplinas_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
  CONSTRAINT fk_usuario_disciplinas_disciplina FOREIGN KEY (id_disciplina) REFERENCES disciplinas(id_disciplina) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notificacoes (
  id_notificacao INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario INT NULL,
  titulo VARCHAR(150) NOT NULL,
  mensagem VARCHAR(500) NOT NULL,
  tipo VARCHAR(40) NOT NULL,
  destino VARCHAR(80) NULL,
  lida BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notificacoes_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
  INDEX idx_notificacoes_usuario_lida (id_usuario, lida, created_at)
);

CREATE TABLE IF NOT EXISTS configuracoes_usuario (
  id_usuario INT PRIMARY KEY,
  tema VARCHAR(30) NOT NULL DEFAULT 'CLARO',
  notificacoes_feed BOOLEAN NOT NULL DEFAULT TRUE,
  notificacoes_chamados BOOLEAN NOT NULL DEFAULT TRUE,
  densidade VARCHAR(20) NOT NULL DEFAULT 'CONFORTAVEL',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_config_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
);

INSERT INTO disciplinas (nome, codigo, professor_nome)
VALUES
  ('Calculo II', 'MAT-CALC2', 'Profa. Juliana Martins'),
  ('Fisica I', 'FIS-001', 'Prof. Ricardo Lopes'),
  ('Programacao Orientada a Objetos', 'COMP-POO', 'Prof. Marcelo Costa'),
  ('Resistencia dos Materiais', 'ENG-RM', 'Profa. Paula Souza')
ON DUPLICATE KEY UPDATE nome = VALUES(nome), professor_nome = VALUES(professor_nome);

INSERT INTO eventos (id_criador, titulo, descricao, local_evento, data_hora, publico_alvo)
SELECT id_usuario, 'Semana Academica', 'Palestras, oficinas e atividades integradas para todos os cursos.', 'Auditorio Central',
       DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY) + INTERVAL 19 HOUR, 'TODOS'
FROM usuarios
WHERE email = 'admin@comunicaluno.com.br'
  AND NOT EXISTS (SELECT 1 FROM eventos WHERE titulo = 'Semana Academica');

INSERT INTO posts (id_autor, tipo_post, titulo, texto, publico_alvo)
SELECT id_usuario, 'AVISO', 'Bem-vindo ao ComunicAluno 3.0',
       'O feed academico agora concentra publicacoes, avisos, comentarios, curtidas e proximos eventos.',
       'TODOS'
FROM usuarios
WHERE email = 'admin@comunicaluno.com.br'
  AND NOT EXISTS (SELECT 1 FROM posts WHERE titulo = 'Bem-vindo ao ComunicAluno 3.0');
