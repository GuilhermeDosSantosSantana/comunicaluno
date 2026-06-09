# ComunicAluno

<<<<<<< HEAD
O ComunicAluno é um sistema desktop desenvolvido em Java para centralizar a comunicação acadêmica entre alunos, professores, coordenadores e administradores. A proposta do projeto é reunir, em um mesmo ambiente, recursos de feed acadêmico, avisos, eventos, cursos, disciplinas, chamados, chat e controle de usuários.

O sistema foi construído com interface em JavaFX e banco de dados MySQL. A estrutura foi separada em camadas para deixar o projeto mais organizado e facilitar a manutenção.

## Contexto do desenvolvimento

No início do projeto, os testes não foram o primeiro foco porque o desenvolvimento foi dividido entre três frentes principais: banco de dados, back-end e front-end.

Enquanto uma parte do grupo estava estruturando o banco, outra parte estava montando o back-end e organizando as regras de negócio. Como as tabelas, relacionamentos e fluxos ainda estavam mudando, os testes não estavam fechados desde o começo, porque testar uma regra que ainda estava sendo definida poderia gerar retrabalho.

A ideia foi primeiro deixar a base do sistema funcionando:

- criar a estrutura do banco de dados;
- definir as tabelas principais;
- montar as classes de modelo;
- criar os DAOs para acesso ao banco;
- montar os serviços com as regras principais;
- conectar as telas aos poucos no front-end.

Depois que a estrutura ficou mais estável, os testes começaram a fazer mais sentido, principalmente para validar regras como login, cadastro, aprovação de conta, cursos, disciplinas, posts, chamados, notificações e conversas.

## Objetivo do sistema

O objetivo do ComunicAluno é melhorar a comunicação dentro do ambiente acadêmico. Em vez de deixar informações espalhadas em vários canais, o sistema centraliza os principais recursos em uma única aplicação.

Entre as principais funções estão:
=====================================

O ComunicAluno é uma aplicação desktop em Java, com interface em JavaFX e banco de dados MySQL, criada para centralizar a comunicação acadêmica entre alunos, professores, coordenadores e administradores.

A proposta do sistema é reunir em um único lugar o feed acadêmico, avisos, notificações, eventos, cursos, disciplinas, chamados, chat e controle de contas.

## Contexto do desenvolvimento

No começo do projeto, os testes não foram a primeira parte a ser fechada porque o grupo dividiu o desenvolvimento em três frentes: banco de dados, back-end e front-end.

Enquanto uma parte estava montando e ajustando o banco de dados, outra parte estava estruturando o back-end, criando models, DAOs, services e regras principais. O front-end foi sendo conectado aos poucos conforme as regras e tabelas ficavam mais estáveis.

Por esse motivo, não faria sentido escrever todos os testes logo no início, porque as tabelas, os relacionamentos e alguns fluxos ainda estavam mudando. Primeiro foi necessário deixar a base funcionando: banco, conexão, regras de negócio e telas principais. Depois disso os testes passaram a fazer mais sentido, porque já existia uma estrutura mais definida para validar.

## Objetivo do sistema

O sistema foi pensado para facilitar a rotina acadêmica. Em vez de deixar as informações espalhadas em vários canais, o ComunicAluno concentra as principais interações em uma única aplicação.

Funções principais:

>>>>>>> 9b05127 (Versão final do Comunica Aluno)
>>>>>>>
>>>>>>
>>>>>
>>>>
>>>
>>

- cadastro e login de usuários;
- aprovação de contas por administrador ou coordenador;
- feed acadêmico com publicações;
  <<<<<<< HEAD
- avisos e notificações;
- cadastro de eventos;
- organização de cursos e disciplinas;
- abertura e acompanhamento de chamados;
- conversas e mensagens internas;
- controle de perfil dos usuários.

## Perfis de usuário

O sistema trabalha com diferentes tipos de usuário. Cada perfil tem permissões diferentes dentro da aplicação.

- Aluno: acessa o feed, visualiza avisos, eventos, cursos, disciplinas, chamados e conversas.
- Professor: participa da comunicação acadêmica, pode interagir com alunos e acompanhar informações relacionadas às disciplinas.
- Coordenador: possui permissões mais amplas, incluindo aprovação de contas e gestão de algumas informações acadêmicas.
- Administrador: possui acesso mais completo ao sistema, podendo gerenciar usuários, cursos e outras áreas administrativas.

## Estrutura geral do projeto

A estrutura principal do projeto está dentro da pasta `src`.
========================================================

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

>>>>>>> 9b05127 (Versão final do Comunica Aluno)
>>>>>>>
>>>>>>
>>>>>
>>>>
>>>
>>

```text
src
├── main
│   ├── java
│   └── resources
└── test
    └── java
```

<<<<<<< HEAD
A pasta `src/main/java` contém o código principal da aplicação.

A pasta `src/main/resources` contém arquivos usados pelo sistema, como imagens, configurações e scripts do banco de dados.

A pasta `src/test/java` contém os testes automatizados do projeto.

## Organização das camadas

O projeto foi separado em camadas para evitar que toda a lógica ficasse misturada nas telas.

### fx

A pasta `fx` concentra a interface gráfica do sistema.

Nela ficam as telas feitas em JavaFX, como login, cadastro, feed, chat, chamados, perfil, contas, cursos, disciplinas, avisos e notificações.

Essa camada é responsável pela parte visual e pela interação com o usuário. Ela não deve concentrar regra pesada de negócio. Quando o usuário clica em um botão ou preenche um formulário, a tela chama a camada de serviço para executar a ação.

### model

A pasta `model` guarda as classes que representam os dados principais do sistema.

Exemplos de modelos:

- Usuario;
- Curso;
- Disciplina;
- Post;
- Evento;
- Chamado;
- Conversa;
- Notificacao.

Essas classes representam os objetos usados dentro da aplicação e, em muitos casos, correspondem às tabelas do banco de dados.

### service

A pasta `service` concentra as regras de negócio.

Ela fica entre a tela e o banco de dados. Quando uma tela precisa cadastrar um usuário, aprovar uma conta, criar um post ou abrir um chamado, ela chama um service.

Essa camada valida as informações antes de chamar o banco. Por exemplo, ela pode verificar se um campo está vazio, se o usuário tem permissão para executar determinada ação ou se um registro existe antes de atualizar.

### dao

A pasta `dao` é responsável pelo acesso ao banco de dados.

DAO significa Data Access Object. Na prática, são as classes que executam os comandos SQL para inserir, buscar, atualizar ou remover informações.

Exemplos:

- UsuarioDAO acessa os dados dos usuários;
- CursoDAO acessa os dados dos cursos;
- DisciplinaDAO acessa os dados das disciplinas;
- PostDAO acessa os dados do feed;
- ChamadoDAO acessa os dados dos chamados;
- ChatDAO acessa os dados das conversas;
- NotificacaoDAO acessa os dados das notificações.

### jdbc

A pasta `jdbc` contém a classe responsável por abrir conexão com o banco de dados.

A conexão usa as informações do arquivo `config.properties`, como endereço do banco, usuário e senha.

### resources

A pasta `resources` guarda arquivos auxiliares usados pelo projeto.

Dentro dela ficam:

- assets visuais, como logo, ícones e imagens padrão;
- scripts SQL do banco de dados;
- arquivo de configuração da conexão com o banco.

### test

A pasta `test` contém os testes automatizados.

Os testes foram pensados principalmente para validar a camada de serviço, porque é nela que ficam as regras principais do sistema.

## Banco de dados

O banco de dados foi estruturado para atender as principais áreas do sistema.

As tabelas cobrem recursos como:
================================

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

>>>>>>> 9b05127 (Versão final do Comunica Aluno)
>>>>>>>
>>>>>>
>>>>>
>>>>
>>>
>>

- usuários;
- cursos;
- disciplinas;
  <<<<<<< HEAD
- vínculo entre usuários e disciplinas;
  =======
- vínculo entre usuário e disciplina;

>>>>>>> 9b05127 (Versão final do Comunica Aluno)
>>>>>>>
>>>>>>
>>>>>
>>>>
>>>
>>

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

<<<<<<< HEAD
Os scripts SQL ficam em:

```text
src/main/resources/db
```

O arquivo `schema.sql` cria a estrutura principal do banco. Os arquivos de upgrade adicionam melhorias e novas tabelas conforme o projeto evoluiu.

## Fluxo básico da aplicação

O fluxo do sistema segue uma estrutura simples:
===============================================

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

>>>>>>> 9b05127 (Versão final do Comunica Aluno)
>>>>>>>
>>>>>>
>>>>>
>>>>
>>>
>>

```text
Tela JavaFX
↓
Service
↓
DAO
↓
Banco de dados
```

<<<<<<< HEAD
Exemplo de login:

1. O usuário informa e-mail e senha na tela.
2. A tela chama o serviço de usuário.
3. O serviço valida os dados.
4. O DAO busca o usuário no banco.
5. O sistema confere a senha.
6. Se os dados estiverem corretos e a conta estiver ativa, o usuário entra no sistema.

Exemplo de aprovação de conta:

1. Um usuário faz cadastro.
2. A conta fica pendente.
3. Um administrador ou coordenador acessa a tela de contas.
4. O responsável aprova ou rejeita a solicitação.
5. O status da conta é atualizado no banco.
6. O usuário aprovado passa a conseguir acessar o sistema.

Exemplo de chamado:

1. O usuário abre um chamado.
2. O sistema registra o chamado no banco.
3. As mensagens ficam vinculadas ao chamado.
4. O status pode ser acompanhado durante o atendimento.

## Funcionalidades atuais

O projeto conta com as seguintes áreas principais:

### Login e cadastro

Permite que usuários criem conta e acessem o sistema. As contas podem depender de aprovação antes do primeiro acesso.

### Contas

Área usada por administradores e coordenadores para aprovar ou inativar usuários.

### Feed acadêmico

Área principal para publicações e interações acadêmicas.

### Avisos e notificações

Área que concentra comunicados e notificações importantes para o usuário.

### Eventos

Área para visualização e gerenciamento de eventos acadêmicos.

### Cursos e disciplinas

Área para organizar os cursos e disciplinas usados no sistema.

### Chamados

Área voltada para solicitações de atendimento e suporte acadêmico.

### Chat

Área para troca de mensagens internas entre usuários.

### Perfil

Área em que o usuário pode consultar e ajustar informações da própria conta.

## Testes

Os testes não foram criados logo no primeiro momento porque o projeto ainda estava passando por definição de banco, telas e regras. Como a estrutura inicial estava mudando bastante, os testes foram deixados para uma etapa em que o sistema já tivesse uma base mais estável.

Com o avanço do projeto, os testes passaram a ser importantes para garantir que as regras principais continuem funcionando, principalmente nas classes de service.

A ideia dos testes é validar pontos como:

- cadastro de usuário;
- login;
- aprovação de contas;
- criação e alteração de cursos;
- criação e alteração de disciplinas;
- criação de posts;
- criação de eventos;
- abertura e atualização de chamados;
- envio e leitura de notificações;
- criação de conversas e mensagens.

## Como configurar o banco

O arquivo de configuração fica em:
====================================

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

>>>>>>> 9b05127 (Versão final do Comunica Aluno)
>>>>>>>
>>>>>>
>>>>>
>>>>
>>>
>>

```text
src/main/resources/config.properties
```

<<<<<<< HEAD
Nele devem ser ajustadas as informações de conexão com o MySQL, conforme o ambiente local.

Exemplo de configuração:
==========================

Exemplo:

>>>>>>> 9b05127 (Versão final do Comunica Aluno)
>>>>>>>
>>>>>>
>>>>>
>>>>
>>>
>>

```properties
db.url=jdbc:mysql://localhost:3306/comunicaluno_db?useSSL=false&serverTimezone=UTC
db.user=root
db.password=root
```

<<<<<<< HEAD
Antes de rodar o sistema, é necessário criar o banco e executar os scripts SQL da pasta `db`.

## Como executar o projeto

No Windows, o projeto pode ser executado pelo Maven Wrapper:
============================================================

Depois execute no MySQL:

```text
src/main/resources/db/schema_final_mock.sql
```

Esse script já cria o banco `comunicaluno_db`, cria as tabelas e adiciona dados iniciais para teste.

## Como executar

No Windows:

>>>>>>> 9b05127 (Versão final do Comunica Aluno)
>>>>>>>
>>>>>>
>>>>>
>>>>
>>>
>>

```bash
mvnw.cmd javafx:run
```

Para compilar:

```bash
mvnw.cmd clean compile
```

<<<<<<< HEAD
Para executar os testes:
========================

Para executar testes:

>>>>>>> 9b05127 (Versão final do Comunica Aluno)
>>>>>>>
>>>>>>
>>>>>
>>>>
>>>
>>

```bash
mvnw.cmd test
```

Também é possível abrir o projeto no Eclipse como projeto Maven.

## Situação atual

<<<<<<< HEAD
O projeto já possui uma base funcional com banco, interface, serviços, DAOs, modelos e testes. Algumas partes foram organizadas aos poucos conforme as telas ficaram prontas e conforme o banco foi sendo consolidado.

A estrutura atual já permite apresentar o sistema como uma aplicação acadêmica com comunicação, chamados, feed, notificações, cursos, disciplinas, eventos e controle de usuários.

Ainda podem ser feitas melhorias futuras, como ajustes visuais, refinamento de permissões, melhoria dos testes e revisão de alguns fluxos para deixar a experiência mais consistente.

## Resumo

O ComunicAluno foi desenvolvido com foco em organizar a comunicação acadêmica em um sistema único. A separação entre banco, back-end e front-end ajudou a dividir o trabalho durante o desenvolvimento, mas também fez com que os testes entrassem com mais força depois que a estrutura principal ficou definida.

A base do projeto está dividida de forma simples: telas no JavaFX, regras nos services, acesso ao banco nos DAOs, dados nos models e scripts SQL nos resources. Essa organização facilita entender o sistema, corrigir problemas e continuar evoluindo o projeto.
====================================================================================================================================================================================================================================================================

O projeto já possui uma base funcional com banco, back-end, front-end, telas conectadas e dados de teste.

As principais melhorias recentes foram:

- criação do script final com mock de dados;
- validação simples de e-mail e senha;
- botão de atualização em telas de feed, chat e chamados;
- tela de post com comentários;
- melhoria visual em botões que estavam com pouco contraste;
- README com jornada de testes do front.

Ainda podem ser feitas melhorias futuras, como refinar permissões, aumentar a cobertura de testes e melhorar alguns detalhes visuais da interface.

>>>>>>> 9b05127 (Versão final do Comunica Aluno)
>>>>>>>
>>>>>>
>>>>>
>>>>
>>>
>>
