# ComunicAluno

O ComunicAluno é uma aplicação desktop em Java, com interface em JavaFX e banco de dados MySQL, criada para centralizar a comunicação acadêmica entre alunos, professores, coordenadores e administradores.

A proposta do sistema é reunir em um único lugar o feed acadêmico, avisos, notificações, eventos, cursos, disciplinas, chamados, chat e controle de contas.

## Contexto do desenvolvimento

No começo do projeto, os testes não foram a primeira parte a ser fechada porque o grupo dividiu o desenvolvimento em três frentes: banco de dados, back-end e front-end.

Enquanto uma parte estava montando e ajustando o banco de dados, outra parte estava estruturando o back-end, criando models, DAOs, services e regras principais. O front-end foi sendo conectado aos poucos conforme as regras e tabelas ficavam mais estáveis.

Por esse motivo, não faria sentido escrever todos os testes logo no início, porque as tabelas, os relacionamentos e alguns fluxos ainda estavam mudando. Primeiro foi necessário deixar a base funcionando: banco, conexão, regras de negócio e telas principais. Depois disso os testes passaram a fazer mais sentido, porque já existia uma estrutura mais definida para validar.

## Objetivo do sistema

O sistema foi pensado para facilitar a rotina acadêmica. Em vez de deixar as informações espalhadas em vários canais, o ComunicAluno concentra as principais interações em uma única aplicação.

Funções principais:

- cadastro e login de usuários;
- aprovação de contas por administrador ou coordenador;
- feed acadêmico com publicações;
- tela de post com comentários;
- avisos e notificações;
- eventos acadêmicos;
- cursos e disciplinas;
- chamados de atendimento;
- chat interno;
- perfil e configurações do usuário.

## Perfis de usuário

O sistema trabalha com quatro perfis principais.

- Aluno: acessa feed, avisos, eventos, cursos, disciplinas, chat, chamados e perfil.
- Professor: participa da comunicação acadêmica, responde interações e acompanha alunos.
- Coordenador: pode aprovar contas, gerenciar cursos e acompanhar áreas administrativas.
- Administrador: tem acesso mais amplo, incluindo gestão de contas, cursos e demais cadastros.

## Estrutura do projeto

A estrutura principal fica dentro de `src`.

```text
src
├── main
│   ├── java
│   └── resources
└── test
    └── java
```

### src/main/java

Contém o código principal da aplicação.

Principais pacotes:

```text
br/com/comunicaluno
├── comunicaluno
├── dao
├── fx
├── jdbc
├── model
└── service
```

### fx

Contém as telas JavaFX do sistema, como login, cadastro, tela principal, feed, chat, chamados, contas, eventos, cursos, disciplinas, avisos, notificações e perfil.

Essa camada cuida da parte visual. Quando o usuário clica em um botão ou preenche um formulário, a tela chama a camada de serviço.

### model

Contém as classes que representam os dados do sistema, como `Usuario`, `Post`, `Curso`, `Disciplina`, `Evento`, `Chamado`, `Conversa` e `Notificacao`.

Essas classes são usadas para transportar os dados entre tela, service, DAO e banco.

### service

Contém as regras de negócio.

Exemplos:

- validar cadastro;
- validar e-mail e senha;
- aprovar conta;
- criar curso;
- criar post;
- comentar publicação;
- abrir chamado;
- enviar mensagem no chat.

### dao

Contém as classes que acessam o banco de dados.

Exemplos:

- `UsuarioDAO` acessa usuários;
- `CursoDAO` acessa cursos;
- `DisciplinaDAO` acessa disciplinas;
- `PostDAO` acessa feed, comentários e curtidas;
- `ChamadoDAO` acessa chamados;
- `ChatDAO` acessa conversas;
- `NotificacaoDAO` acessa notificações.

### jdbc

Contém a `ConnectionFactory`, responsável por abrir conexão com o MySQL usando o arquivo `config.properties`.

### src/main/resources

Guarda arquivos auxiliares do projeto.

```text
resources
├── assets
├── db
└── config.properties
```

`assets` guarda imagens, ícones e placeholders usados na interface.

`db` guarda os scripts SQL.

`config.properties` guarda as informações de conexão com o banco.

## Banco de dados

O banco usado pelo projeto é o MySQL.

As tabelas principais cobrem:

- usuários;
- cursos;
- disciplinas;
- vínculo entre usuário e disciplina;
- posts;
- comentários;
- curtidas;
- eventos;
- chamados;
- mensagens de chamados;
- conversas;
- participantes de conversas;
- mensagens de conversas;
- notificações;
- configurações de usuário.

O script mais completo para subir o banco do zero com dados iniciais é:

```text
src/main/resources/db/schema_final_mock.sql
```

Esse arquivo cria as tabelas finais e também insere dados para teste, como usuários, cursos, disciplinas, posts, comentários, eventos, chamados, chat e notificações.

Também existem scripts separados de evolução do banco:

```text
schema.sql
upgrade_v2_social.sql
upgrade_v3_academico.sql
upgrade_v4_chat_exclusao.sql
```

Para uma instalação nova, o mais simples é executar o `schema_final_mock.sql`.

## Usuários de teste

Todos os usuários abaixo usam a senha:

```text
admin123
```

Contas disponíveis no mock:

```text
admin@comunicaluno.com.br      ADMIN
coord@comunicaluno.com.br      COORDENADOR
professor@comunicaluno.com.br  PROF
aluno@comunicaluno.com.br      ALUNO
aluno2@comunicaluno.com.br     ALUNO
pendente@comunicaluno.com.br   ALUNO pendente
```

A conta `pendente@comunicaluno.com.br` serve para testar a tela de aprovação de contas.

## Validação de e-mail e senha

O cadastro possui uma validação simples antes de enviar os dados para o banco.

O e-mail precisa seguir um formato básico, como:

```text
nome@dominio.com
```

A senha precisa ter:

- pelo menos 6 caracteres;
- pelo menos uma letra;
- pelo menos um número.

Essa validação existe no front e também na camada de serviço, para evitar que dados inválidos sejam enviados diretamente ao banco.

## Jornada para testar o sistema

Abaixo está um roteiro simples para testar as telas principais do front-end.

### 1. Login

Acesse a tela inicial e entre com:

```text
admin@comunicaluno.com.br
admin123
```

Esse usuário tem permissão para aprovar contas e acessar áreas administrativas.

### 2. Cadastro de usuário

Na tela de login, clique em criar nova conta.

Preencha nome, e-mail, senha, perfil, curso e turma.

Teste também um e-mail inválido e uma senha fraca para confirmar a validação.

Depois de cadastrar, a conta deve ficar pendente até ser aprovada.

### 3. Contas

Entre como admin ou coordenador.

Acesse o menu `Contas`.

Selecione um usuário pendente e clique em aprovar.

Depois disso, tente acessar o sistema com o usuário aprovado.

### 4. Feed acadêmico

Acesse o menu `Inicial`.

Teste as ações principais:

- criar uma nova publicação;
- curtir uma publicação;
- abrir uma publicação;
- comentar dentro da tela do post;
- clicar em `Atualizar página` para buscar novos posts.

A tela de post funciona parecida com uma publicação aberta em rede social: ela mostra o conteúdo principal e os comentários abaixo.

Quando o usuário abre a publicação, o sistema carrega os dados atualizados daquele post. Se a tela do post já estiver aberta e novos comentários forem feitos, o usuário pode clicar em `Atualizar página` dentro da própria tela do post.

### 5. Avisos e notificações

Acesse `Avisos e Notificações`.

Use os botões internos para alternar entre avisos e notificações.

Teste:

- visualizar avisos;
- atualizar a lista de avisos;
- visualizar notificações;
- marcar notificações como lidas;
- atualizar a lista de notificações.

### 6. Eventos

Acesse `Eventos`.

Com admin, coordenador ou professor, teste a criação de um evento.

Depois confira se o evento aparece na listagem.

### 7. Cursos e disciplinas

Acesse `Cursos e Disciplinas`.

Use os botões internos para alternar entre cursos e disciplinas.

Como admin ou coordenador, teste:

- criar curso;
- editar curso;
- inativar curso;
- reativar curso;
- criar disciplina.

Como aluno, apenas consulte as informações disponíveis.

### 8. Chat

Acesse `Chat`.

Teste:

- abrir uma conversa;
- enviar mensagem;
- criar grupo, usando um usuário com permissão;
- adicionar ou remover alunos de um grupo;
- clicar em `Atualizar página` para buscar novas mensagens.

### 9. Chamados

Acesse `Chamados`.

Como aluno, abra um chamado novo.

Como professor, coordenador ou admin, teste:

- assumir chamado;
- alterar status;
- responder chamado;
- anexar arquivo;
- clicar em `Atualizar página` para buscar novas mensagens.

### 10. Perfil e configurações

Acesse `Perfil`.

Nessa tela ficam juntos os dados do perfil e as configurações básicas.

Teste:

- alterar nome;
- alterar curso;
- alterar turma;
- alterar avatar;
- salvar preferências de notificação.

## Fluxo interno da aplicação

O fluxo principal segue esta lógica:

```text
Tela JavaFX
↓
Service
↓
DAO
↓
Banco de dados
```

Exemplo de cadastro:

1. A tela recebe os dados do usuário.
2. O service valida e-mail, senha, perfil e duplicidade.
3. O DAO grava o usuário no banco.
4. A conta fica com status pendente.
5. Um admin ou coordenador aprova a conta.

Exemplo de publicação:

1. O usuário cria um post no feed.
2. A tela envia os dados para o `PostService`.
3. O service valida o conteúdo.
4. O `PostDAO` grava no banco.
5. O feed pode ser atualizado pelo botão de atualizar página.

Exemplo de comentário:

1. O usuário abre uma publicação.
2. A tela carrega o post atualizado e seus comentários.
3. O usuário escreve um comentário.
4. O comentário é salvo no banco.
5. A tela do post é recarregada.

## Como configurar o banco

Edite o arquivo:

```text
src/main/resources/config.properties
```

Exemplo:

```properties
db.url=jdbc:mysql://localhost:3306/comunicaluno_db?useSSL=false&serverTimezone=UTC
db.user=root
db.password=root
```

Depois execute no MySQL:

```text
src/main/resources/db/schema_final_mock.sql
```

Esse script já cria o banco `comunicaluno_db`, cria as tabelas e adiciona dados iniciais para teste.

## Como executar

No Windows:

```bash
mvnw.cmd javafx:run
```

Para compilar:

```bash
mvnw.cmd clean compile
```

Para executar testes:

```bash
mvnw.cmd test
```

Também é possível abrir o projeto no Eclipse como projeto Maven.

## Situação atual

O projeto já possui uma base funcional com banco, back-end, front-end, telas conectadas e dados de teste.

As principais melhorias recentes foram:

- criação do script final com mock de dados;
- validação simples de e-mail e senha;
- botão de atualização em telas de feed, chat e chamados;
- tela de post com comentários;
- melhoria visual em botões que estavam com pouco contraste;
- README com jornada de testes do front.

Ainda podem ser feitas melhorias futuras, como refinar permissões, aumentar a cobertura de testes e melhorar alguns detalhes visuais da interface.
