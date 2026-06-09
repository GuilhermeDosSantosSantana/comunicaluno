CREATE DATABASE IF NOT EXISTS comunicaluno_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE comunicaluno_db;

CREATE TABLE IF NOT EXISTS usuarios (
  id_usuario INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(120) NOT NULL,
  email VARCHAR(160) NOT NULL,
  senha_hash VARCHAR(100) NOT NULL,
  tipo_perfil ENUM('ADMIN', 'COORDENADOR', 'PROF', 'ALUNO') NOT NULL,
  status_conta ENUM('PENDENTE', 'ATIVO', 'INATIVO') NOT NULL DEFAULT 'PENDENTE',
  avatar_path VARCHAR(500) NULL,
  turma VARCHAR(80) NULL,
  curso VARCHAR(120) NULL,
  aprovado_por INT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_usuarios_email UNIQUE (email),
  CONSTRAINT fk_usuarios_aprovado_por
    FOREIGN KEY (aprovado_por) REFERENCES usuarios(id_usuario)
    ON DELETE SET NULL
);

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

CREATE TABLE IF NOT EXISTS comunicados (
  id_comunicado INT AUTO_INCREMENT PRIMARY KEY,
  id_autor INT NOT NULL,
  titulo VARCHAR(150) NOT NULL,
  mensagem TEXT NOT NULL,
  destinatario_tipo ENUM('TODOS', 'ALUNOS', 'PROFESSORES', 'COORDENADORES') NOT NULL,
  imagem_path VARCHAR(500) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_comunicados_autor
    FOREIGN KEY (id_autor) REFERENCES usuarios(id_usuario)
    ON DELETE RESTRICT,
  INDEX idx_comunicados_destinatario_created (destinatario_tipo, created_at)
);

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

CREATE TABLE IF NOT EXISTS chamados (
  id_chamado INT AUTO_INCREMENT PRIMARY KEY,
  id_aluno INT NOT NULL,
  id_responsavel INT NULL,
  assunto VARCHAR(150) NOT NULL,
  descricao TEXT NOT NULL,
  anexo_path VARCHAR(500) NULL,
  status ENUM('ABERTO', 'EM_ANALISE', 'RESOLVIDO', 'FECHADO') NOT NULL DEFAULT 'ABERTO',
  deleted_at TIMESTAMP NULL,
  deleted_by INT NULL,
  delete_reason VARCHAR(255) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_chamados_aluno
    FOREIGN KEY (id_aluno) REFERENCES usuarios(id_usuario)
    ON DELETE RESTRICT,
  CONSTRAINT fk_chamados_responsavel
    FOREIGN KEY (id_responsavel) REFERENCES usuarios(id_usuario)
    ON DELETE SET NULL,
  CONSTRAINT fk_chamados_deleted_by
    FOREIGN KEY (deleted_by) REFERENCES usuarios(id_usuario)
    ON DELETE SET NULL,
  INDEX idx_chamados_aluno_created (id_aluno, created_at),
  INDEX idx_chamados_status_created (status, created_at),
  INDEX idx_chamados_deleted (deleted_at)
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

CREATE TABLE IF NOT EXISTS posts (
  id_post INT AUTO_INCREMENT PRIMARY KEY,
  id_autor INT NOT NULL,
  tipo_post ENUM('PUBLICACAO', 'AVISO', 'EVENTO') NOT NULL DEFAULT 'PUBLICACAO',
  titulo VARCHAR(160) NULL,
  texto TEXT NOT NULL,
  publico_alvo ENUM('TODOS', 'ALUNOS', 'PROFESSORES', 'COORDENADORES') NOT NULL DEFAULT 'TODOS',
  imagem_path VARCHAR(500) NULL,
  anexo_path VARCHAR(500) NULL,
  deleted_at TIMESTAMP NULL,
  deleted_by INT NULL,
  delete_reason VARCHAR(255) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_posts_autor
    FOREIGN KEY (id_autor) REFERENCES usuarios(id_usuario)
    ON DELETE RESTRICT,
  CONSTRAINT fk_posts_deleted_by
    FOREIGN KEY (deleted_by) REFERENCES usuarios(id_usuario)
    ON DELETE SET NULL,
  INDEX idx_posts_publico_created (publico_alvo, created_at),
  INDEX idx_posts_autor_created (id_autor, created_at),
  INDEX idx_posts_deleted (deleted_at)
);

CREATE TABLE IF NOT EXISTS post_comentarios (
  id_comentario INT AUTO_INCREMENT PRIMARY KEY,
  id_post INT NOT NULL,
  id_autor INT NOT NULL,
  comentario TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_post_comentarios_post
    FOREIGN KEY (id_post) REFERENCES posts(id_post)
    ON DELETE CASCADE,
  CONSTRAINT fk_post_comentarios_autor
    FOREIGN KEY (id_autor) REFERENCES usuarios(id_usuario)
    ON DELETE RESTRICT,
  INDEX idx_post_comentarios_created (id_post, created_at)
);

CREATE TABLE IF NOT EXISTS post_curtidas (
  id_post INT NOT NULL,
  id_usuario INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_post, id_usuario),
  CONSTRAINT fk_post_curtidas_post
    FOREIGN KEY (id_post) REFERENCES posts(id_post)
    ON DELETE CASCADE,
  CONSTRAINT fk_post_curtidas_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS post_salvos (
  id_post INT NOT NULL,
  id_usuario INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_post, id_usuario),
  CONSTRAINT fk_post_salvos_post
    FOREIGN KEY (id_post) REFERENCES posts(id_post)
    ON DELETE CASCADE,
  CONSTRAINT fk_post_salvos_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
    ON DELETE CASCADE
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
  deleted_at TIMESTAMP NULL,
  deleted_by INT NULL,
  delete_reason VARCHAR(255) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_eventos_criador
    FOREIGN KEY (id_criador) REFERENCES usuarios(id_usuario)
    ON DELETE RESTRICT,
  CONSTRAINT fk_eventos_deleted_by
    FOREIGN KEY (deleted_by) REFERENCES usuarios(id_usuario)
    ON DELETE SET NULL,
  INDEX idx_eventos_publico_data (publico_alvo, data_hora),
  INDEX idx_eventos_deleted (deleted_at)
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
  CONSTRAINT fk_usuario_disciplinas_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
    ON DELETE CASCADE,
  CONSTRAINT fk_usuario_disciplinas_disciplina
    FOREIGN KEY (id_disciplina) REFERENCES disciplinas(id_disciplina)
    ON DELETE CASCADE
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
  CONSTRAINT fk_conversas_curso
    FOREIGN KEY (id_curso) REFERENCES cursos(id_curso)
    ON DELETE SET NULL,
  CONSTRAINT fk_conversas_disciplina
    FOREIGN KEY (id_disciplina) REFERENCES disciplinas(id_disciplina)
    ON DELETE SET NULL,
  CONSTRAINT fk_conversas_professor
    FOREIGN KEY (id_professor_responsavel) REFERENCES usuarios(id_usuario)
    ON DELETE SET NULL,
  CONSTRAINT fk_conversas_created_by
    FOREIGN KEY (created_by) REFERENCES usuarios(id_usuario)
    ON DELETE RESTRICT,
  CONSTRAINT fk_conversas_deleted_by
    FOREIGN KEY (deleted_by) REFERENCES usuarios(id_usuario)
    ON DELETE SET NULL,
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
  CONSTRAINT fk_conversa_participantes_conversa
    FOREIGN KEY (id_conversa) REFERENCES conversas(id_conversa)
    ON DELETE CASCADE,
  CONSTRAINT fk_conversa_participantes_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
    ON DELETE CASCADE,
  CONSTRAINT fk_conversa_participantes_added_by
    FOREIGN KEY (added_by) REFERENCES usuarios(id_usuario)
    ON DELETE SET NULL,
  CONSTRAINT fk_conversa_participantes_removed_by
    FOREIGN KEY (removed_by) REFERENCES usuarios(id_usuario)
    ON DELETE SET NULL,
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
  CONSTRAINT fk_conversa_mensagens_conversa
    FOREIGN KEY (id_conversa) REFERENCES conversas(id_conversa)
    ON DELETE CASCADE,
  CONSTRAINT fk_conversa_mensagens_autor
    FOREIGN KEY (id_autor) REFERENCES usuarios(id_usuario)
    ON DELETE RESTRICT,
  CONSTRAINT fk_conversa_mensagens_deleted_by
    FOREIGN KEY (deleted_by) REFERENCES usuarios(id_usuario)
    ON DELETE SET NULL,
  INDEX idx_conversa_mensagens_created (id_conversa, created_at),
  INDEX idx_conversa_mensagens_deleted (deleted_at)
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
  CONSTRAINT fk_notificacoes_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
    ON DELETE CASCADE,
  INDEX idx_notificacoes_usuario_lida (id_usuario, lida, created_at)
);

CREATE TABLE IF NOT EXISTS configuracoes_usuario (
  id_usuario INT PRIMARY KEY,
  tema VARCHAR(30) NOT NULL DEFAULT 'CLARO',
  notificacoes_feed BOOLEAN NOT NULL DEFAULT TRUE,
  notificacoes_chamados BOOLEAN NOT NULL DEFAULT TRUE,
  densidade VARCHAR(20) NOT NULL DEFAULT 'CONFORTAVEL',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_config_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
    ON DELETE CASCADE
);

INSERT INTO usuarios (nome, email, senha_hash, tipo_perfil, status_conta)
VALUES (
  'Administrador ComunicAluno',
  'admin@comunicaluno.com.br',
  '$2a$12$3JSmIRZKIHzF4zgycMPEQ.WzjbIDjdIIIcnfZ8do1sBK8oEDzOVVW',
  'ADMIN',
  'ATIVO'
)
ON DUPLICATE KEY UPDATE
  nome = VALUES(nome),
  senha_hash = VALUES(senha_hash),
  tipo_perfil = VALUES(tipo_perfil),
  status_conta = VALUES(status_conta);

INSERT INTO cursos (nome, codigo)
VALUES
  ('Engenharia da Computacao', 'ENG-COMP'),
  ('Engenharia Civil', 'ENG-CIVIL'),
  ('Administracao', 'ADM'),
  ('Direito', 'DIR'),
  ('Medicina', 'MED'),
  ('Analise e Desenvolvimento de Sistemas', 'ADS')
ON DUPLICATE KEY UPDATE
  nome = VALUES(nome),
  ativo = TRUE;

INSERT INTO disciplinas (nome, codigo, professor_nome)
VALUES
  ('Calculo II', 'MAT-CALC2', 'Profa. Juliana Martins'),
  ('Fisica I', 'FIS-001', 'Prof. Ricardo Lopes'),
  ('Programacao Orientada a Objetos', 'COMP-POO', 'Prof. Marcelo Costa'),
  ('Resistencia dos Materiais', 'ENG-RM', 'Profa. Paula Souza')
ON DUPLICATE KEY UPDATE
  nome = VALUES(nome),
  professor_nome = VALUES(professor_nome);

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

-- Dados adicionais para testes manuais do front-end
-- Senha padrao dos usuarios abaixo: admin123

INSERT INTO usuarios (nome, email, senha_hash, tipo_perfil, status_conta, turma, curso)
VALUES
  ('Coordenacao Academica', 'coord@comunicaluno.com.br', '$2a$12$3JSmIRZKIHzF4zgycMPEQ.WzjbIDjdIIIcnfZ8do1sBK8oEDzOVVW', 'COORDENADOR', 'ATIVO', NULL, 'Engenharia da Computacao'),
  ('Professor Marcelo Costa', 'professor@comunicaluno.com.br', '$2a$12$3JSmIRZKIHzF4zgycMPEQ.WzjbIDjdIIIcnfZ8do1sBK8oEDzOVVW', 'PROF', 'ATIVO', NULL, 'Engenharia da Computacao'),
  ('Ana Paula Santos', 'aluno@comunicaluno.com.br', '$2a$12$3JSmIRZKIHzF4zgycMPEQ.WzjbIDjdIIIcnfZ8do1sBK8oEDzOVVW', 'ALUNO', 'ATIVO', 'EC3A', 'Engenharia da Computacao'),
  ('Bruno Oliveira', 'aluno2@comunicaluno.com.br', '$2a$12$3JSmIRZKIHzF4zgycMPEQ.WzjbIDjdIIIcnfZ8do1sBK8oEDzOVVW', 'ALUNO', 'ATIVO', 'EC3A', 'Engenharia da Computacao'),
  ('Usuario Aguardando Aprovacao', 'pendente@comunicaluno.com.br', '$2a$12$3JSmIRZKIHzF4zgycMPEQ.WzjbIDjdIIIcnfZ8do1sBK8oEDzOVVW', 'ALUNO', 'PENDENTE', 'EC1A', 'Analise e Desenvolvimento de Sistemas')
ON DUPLICATE KEY UPDATE
  nome = VALUES(nome),
  senha_hash = VALUES(senha_hash),
  tipo_perfil = VALUES(tipo_perfil),
  status_conta = VALUES(status_conta),
  turma = VALUES(turma),
  curso = VALUES(curso);

INSERT INTO usuario_disciplinas (id_usuario, id_disciplina, progresso_percentual)
SELECT u.id_usuario, d.id_disciplina,
       CASE d.codigo
         WHEN 'COMP-POO' THEN 65
         WHEN 'MAT-CALC2' THEN 40
         ELSE 20
       END
FROM usuarios u
JOIN disciplinas d ON d.codigo IN ('COMP-POO', 'MAT-CALC2', 'FIS-001')
WHERE u.email IN ('aluno@comunicaluno.com.br', 'aluno2@comunicaluno.com.br')
ON DUPLICATE KEY UPDATE progresso_percentual = VALUES(progresso_percentual);

INSERT INTO posts (id_autor, tipo_post, titulo, texto, publico_alvo)
SELECT u.id_usuario, 'PUBLICACAO', NULL,
       'Alguem conseguiu finalizar a atividade de POO? Estou revisando heranca, interface e polimorfismo.',
       'TODOS'
FROM usuarios u
WHERE u.email = 'aluno@comunicaluno.com.br'
  AND NOT EXISTS (SELECT 1 FROM posts WHERE texto LIKE 'Alguem conseguiu finalizar a atividade de POO%');

INSERT INTO posts (id_autor, tipo_post, titulo, texto, publico_alvo)
SELECT u.id_usuario, 'PUBLICACAO', NULL,
       'Material da aula de banco de dados ja esta disponivel. Vale revisar os relacionamentos antes da entrega.',
       'TODOS'
FROM usuarios u
WHERE u.email = 'professor@comunicaluno.com.br'
  AND NOT EXISTS (SELECT 1 FROM posts WHERE texto LIKE 'Material da aula de banco de dados%');

INSERT INTO posts (id_autor, tipo_post, titulo, texto, publico_alvo)
SELECT u.id_usuario, 'AVISO', 'Prazo de entrega do projeto',
       'A entrega final do projeto deve conter banco, back-end, front-end e testes basicos de navegacao.',
       'TODOS'
FROM usuarios u
WHERE u.email = 'coord@comunicaluno.com.br'
  AND NOT EXISTS (SELECT 1 FROM posts WHERE titulo = 'Prazo de entrega do projeto');

INSERT INTO post_comentarios (id_post, id_autor, comentario)
SELECT p.id_post, u.id_usuario, 'Eu tambem estou revisando essa parte. A tela de post ajuda a acompanhar os comentarios.'
FROM posts p
JOIN usuarios u ON u.email = 'aluno2@comunicaluno.com.br'
WHERE p.texto LIKE 'Alguem conseguiu finalizar a atividade de POO%'
  AND NOT EXISTS (
    SELECT 1 FROM post_comentarios pc
    WHERE pc.id_post = p.id_post AND pc.id_autor = u.id_usuario
  );

INSERT INTO post_comentarios (id_post, id_autor, comentario)
SELECT p.id_post, u.id_usuario, 'Vou separar alguns exemplos para a turma testar no sistema.'
FROM posts p
JOIN usuarios u ON u.email = 'professor@comunicaluno.com.br'
WHERE p.texto LIKE 'Alguem conseguiu finalizar a atividade de POO%'
  AND NOT EXISTS (
    SELECT 1 FROM post_comentarios pc
    WHERE pc.id_post = p.id_post AND pc.id_autor = u.id_usuario AND pc.comentario LIKE 'Vou separar alguns exemplos%'
  );

INSERT IGNORE INTO post_curtidas (id_post, id_usuario)
SELECT p.id_post, u.id_usuario
FROM posts p
JOIN usuarios u ON u.email IN ('aluno@comunicaluno.com.br', 'aluno2@comunicaluno.com.br', 'professor@comunicaluno.com.br')
WHERE p.deleted_at IS NULL;

INSERT INTO eventos (id_criador, titulo, descricao, local_evento, data_hora, publico_alvo)
SELECT u.id_usuario, 'Apresentacao dos projetos',
       'Cada grupo deve demonstrar o fluxo principal do sistema e explicar a divisao entre banco, back e front.',
       'Laboratorio de Informatica', DATE_ADD(CURRENT_DATE, INTERVAL 10 DAY) + INTERVAL 20 HOUR, 'TODOS'
FROM usuarios u
WHERE u.email = 'coord@comunicaluno.com.br'
  AND NOT EXISTS (SELECT 1 FROM eventos WHERE titulo = 'Apresentacao dos projetos');

INSERT INTO chamados (id_aluno, id_responsavel, assunto, descricao, status)
SELECT aluno.id_usuario, prof.id_usuario, 'Duvida sobre entrega do projeto',
       'Preciso confirmar se o script final do banco deve ser entregue junto com o README.',
       'EM_ANALISE'
FROM usuarios aluno
JOIN usuarios prof ON prof.email = 'professor@comunicaluno.com.br'
WHERE aluno.email = 'aluno@comunicaluno.com.br'
  AND NOT EXISTS (SELECT 1 FROM chamados WHERE assunto = 'Duvida sobre entrega do projeto');

INSERT INTO chamado_mensagens (id_chamado, id_autor, mensagem)
SELECT c.id_chamado, u.id_usuario, 'Abri este chamado para validar o fluxo de suporte.'
FROM chamados c
JOIN usuarios u ON u.email = 'aluno@comunicaluno.com.br'
WHERE c.assunto = 'Duvida sobre entrega do projeto'
  AND NOT EXISTS (SELECT 1 FROM chamado_mensagens WHERE id_chamado = c.id_chamado AND id_autor = u.id_usuario);

INSERT INTO chamado_mensagens (id_chamado, id_autor, mensagem)
SELECT c.id_chamado, u.id_usuario, 'Sim, deixe o script final e o mock documentados no README.'
FROM chamados c
JOIN usuarios u ON u.email = 'professor@comunicaluno.com.br'
WHERE c.assunto = 'Duvida sobre entrega do projeto'
  AND NOT EXISTS (SELECT 1 FROM chamado_mensagens WHERE id_chamado = c.id_chamado AND id_autor = u.id_usuario);

INSERT INTO conversas (tipo, nome, id_curso, id_disciplina, id_professor_responsavel, created_by)
SELECT 'GRUPO_MATERIA', 'Grupo de POO', NULL, d.id_disciplina, prof.id_usuario, prof.id_usuario
FROM disciplinas d
JOIN usuarios prof ON prof.email = 'professor@comunicaluno.com.br'
WHERE d.codigo = 'COMP-POO'
  AND NOT EXISTS (SELECT 1 FROM conversas WHERE nome = 'Grupo de POO');

INSERT INTO conversa_participantes (id_conversa, id_usuario, papel, ativo, added_by)
SELECT c.id_conversa, u.id_usuario,
       CASE WHEN u.email = 'professor@comunicaluno.com.br' THEN 'PROF_RESPONSAVEL' ELSE 'MEMBRO' END,
       TRUE,
       c.created_by
FROM conversas c
JOIN usuarios u ON u.email IN ('professor@comunicaluno.com.br', 'aluno@comunicaluno.com.br', 'aluno2@comunicaluno.com.br')
WHERE c.nome = 'Grupo de POO'
ON DUPLICATE KEY UPDATE ativo = TRUE, papel = VALUES(papel);

INSERT INTO conversa_mensagens (id_conversa, id_autor, mensagem)
SELECT c.id_conversa, u.id_usuario, 'Grupo criado para tirar duvidas da disciplina e testar o chat.'
FROM conversas c
JOIN usuarios u ON u.email = 'professor@comunicaluno.com.br'
WHERE c.nome = 'Grupo de POO'
  AND NOT EXISTS (SELECT 1 FROM conversa_mensagens WHERE id_conversa = c.id_conversa AND id_autor = u.id_usuario);

INSERT INTO notificacoes (id_usuario, titulo, mensagem, tipo, destino, lida)
SELECT u.id_usuario, 'Bem-vindo ao ComunicAluno',
       'Use este usuario para testar feed, avisos, notificacoes, chat, chamados, cursos e perfil.',
       'SISTEMA', 'INICIAL', FALSE
FROM usuarios u
WHERE u.email IN ('aluno@comunicaluno.com.br', 'professor@comunicaluno.com.br', 'coord@comunicaluno.com.br')
  AND NOT EXISTS (
    SELECT 1 FROM notificacoes n
    WHERE n.id_usuario = u.id_usuario AND n.titulo = 'Bem-vindo ao ComunicAluno'
  );

