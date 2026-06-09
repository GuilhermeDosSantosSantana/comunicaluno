package br.com.comunicaluno.fx;

import br.com.comunicaluno.model.Conversa;
import br.com.comunicaluno.model.ConversaMensagem;
import br.com.comunicaluno.model.ConversaParticipante;
import br.com.comunicaluno.model.Curso;
import br.com.comunicaluno.model.Disciplina;
import br.com.comunicaluno.model.GrupoAcademico;
import br.com.comunicaluno.model.Usuario;
import br.com.comunicaluno.service.ChatService;
import br.com.comunicaluno.service.CursoService;
import br.com.comunicaluno.service.DisciplinaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ChatFxView extends BorderPane {

    private static final Set<String> GESTORES = Set.of("ADMIN", "COORDENADOR");

    private final Usuario usuarioLogado;
    private final Runnable onDataChanged;
    private final ChatService chatService = new ChatService();
    private final CursoService cursoService = new CursoService();
    private final DisciplinaService disciplinaService = new DisciplinaService();
    private final ObservableList<Conversa> conversas = FXCollections.observableArrayList();
    private final ListView<Conversa> listaConversas = new ListView<>(conversas);
    private final VBox mensagensBox = new VBox(10);
    private final Label tituloConversa = FxTheme.title("Selecione uma conversa", 18);
    private final Label detalhesConversa = FxTheme.muted("");
    private final TextField mensagemField = new TextField();
    private final ListView<ConversaParticipante> participantes = new ListView<>();
    private Conversa conversaAtual;

    public ChatFxView(Usuario usuarioLogado, Runnable onDataChanged) {
        this.usuarioLogado = usuarioLogado;
        this.onDataChanged = onDataChanged;
        setPadding(new Insets(18, 22, 22, 22));
        setStyle("-fx-background-color: " + FxTheme.FUNDO + ";");
        setLeft(criarLista());
        setCenter(criarMensagens());
        setRight(criarPainelAcoes());
        carregarConversas();
    }

    private VBox criarLista() {
        VBox box = FxTheme.card(10);
        box.setPrefWidth(330);
        box.setMaxWidth(330);
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button atualizar = FxTheme.secondaryButton("Atualizar");
        atualizar.setOnAction(e -> atualizarTela());
        header.getChildren().addAll(FxTheme.title("Chat", 18), spacer, atualizar);
        box.getChildren().add(header);
        listaConversas.setPrefHeight(650);
        listaConversas.setStyle("-fx-background-color: " + FxTheme.CARD + "; -fx-control-inner-background: "
                + FxTheme.CARD + "; -fx-border-color: " + FxTheme.BORDA + "; -fx-border-radius: 8;");
        listaConversas.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Conversa item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: " + FxTheme.CARD + ";");
                } else {
                    setText(item.getNome() + "\n" + tipoConversa(item));
                    setStyle("-fx-background-color: " + FxTheme.CARD + "; -fx-text-fill: " + FxTheme.TEXTO + "; "
                            + "-fx-padding: 10; -fx-border-color: transparent transparent " + FxTheme.BORDA + " transparent;");
                }
            }
        });
        listaConversas.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> selecionarConversa(newValue));
        box.getChildren().add(listaConversas);
        return box;
    }

    private BorderPane criarMensagens() {
        BorderPane centro = new BorderPane();
        centro.setPadding(new Insets(0, 16, 0, 16));

        VBox topo = FxTheme.card(6);
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titulos = new VBox(2, tituloConversa, detalhesConversa);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button atualizar = FxTheme.secondaryButton("Atualizar página");
        atualizar.setOnAction(e -> atualizarTela());
        header.getChildren().addAll(titulos, spacer, atualizar);
        topo.getChildren().add(header);
        centro.setTop(topo);

        mensagensBox.setPadding(new Insets(16, 0, 16, 0));
        ScrollPane scroll = new ScrollPane(mensagensBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        centro.setCenter(scroll);

        HBox envio = new HBox(8);
        envio.setPadding(new Insets(12, 0, 0, 0));
        mensagemField.setPromptText("Digite sua mensagem...");
        mensagemField.setStyle(FxTheme.inputStyle());
        HBox.setHgrow(mensagemField, Priority.ALWAYS);
        Button enviar = FxTheme.primaryButton("Enviar");
        enviar.setOnAction(e -> enviarMensagem());
        envio.getChildren().addAll(mensagemField, enviar);
        centro.setBottom(envio);
        return centro;
    }

    private VBox criarPainelAcoes() {
        VBox painel = FxTheme.card(12);
        painel.setPrefWidth(360);
        painel.setMaxWidth(360);
        painel.getChildren().add(FxTheme.title("Participantes", 16));
        participantes.setPrefHeight(160);
        participantes.setStyle("-fx-background-color: " + FxTheme.CARD + "; -fx-control-inner-background: "
                + FxTheme.CARD + "; -fx-border-color: " + FxTheme.BORDA + "; -fx-border-radius: 8;");
        participantes.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(ConversaParticipante item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNomeUsuario() + " - " + item.getPapel());
                setStyle("-fx-background-color: " + FxTheme.CARD + "; -fx-text-fill: " + FxTheme.TEXTO + "; -fx-padding: 8;");
            }
        });
        painel.getChildren().add(participantes);

        if ("ALUNO".equals(usuarioLogado.getTipoPerfil())) {
            painel.getChildren().add(criarPrivadoAluno());
        }
        if (GESTORES.contains(usuarioLogado.getTipoPerfil())) {
            painel.getChildren().add(criarFormularioGrupo());
        }
        if (!"ALUNO".equals(usuarioLogado.getTipoPerfil())) {
            painel.getChildren().add(criarGestaoMembros());
        }
        return painel;
    }

    private VBox criarPrivadoAluno() {
        VBox box = new VBox(8);
        box.getChildren().add(FxTheme.title("Conversa privada", 15));
        ComboBox<Usuario> alunos = new ComboBox<>();
        configurarComboUsuario(alunos);
        try {
            for (Usuario aluno : chatService.listarAlunosAtivos(usuarioLogado)) {
                if (!usuarioLogado.getIdUsuario().equals(aluno.getIdUsuario())) {
                    alunos.getItems().add(aluno);
                }
            }
        } catch (Exception ex) {
            box.getChildren().add(FxTheme.muted("Alunos indisponíveis agora."));
        }
        Button iniciar = FxTheme.primaryButton("Iniciar conversa");
        iniciar.setMaxWidth(Double.MAX_VALUE);
        iniciar.setOnAction(e -> {
            Usuario aluno = alunos.getValue();
            if (aluno == null) {
                FxDialogs.erro("Selecione um aluno.");
                return;
            }
            try {
                chatService.iniciarConversaPrivada(aluno.getIdUsuario(), usuarioLogado);
                carregarConversas();
            } catch (Exception ex) {
                FxDialogs.erro(ex.getMessage());
            }
        });
        box.getChildren().addAll(alunos, iniciar);
        return box;
    }

    private VBox criarFormularioGrupo() {
        VBox box = new VBox(8);
        box.getChildren().add(FxTheme.title("Criar grupo", 15));
        TextField nome = new TextField();
        nome.setPromptText("Nome do grupo");
        nome.setStyle(FxTheme.inputStyle());

        ComboBox<String> tipo = new ComboBox<>();
        tipo.getItems().addAll("GRUPO_MATERIA", "GRUPO_CURSO");
        tipo.setValue("GRUPO_MATERIA");
        tipo.setStyle(FxTheme.inputStyle());

        ComboBox<Curso> cursos = new ComboBox<>();
        cursos.setPromptText("Curso");
        cursos.setStyle(FxTheme.inputStyle());
        carregarCursos(cursos);

        ComboBox<Disciplina> disciplinas = new ComboBox<>();
        disciplinas.setPromptText("Matéria");
        disciplinas.setStyle(FxTheme.inputStyle());
        carregarDisciplinas(disciplinas);

        ComboBox<Usuario> professor = new ComboBox<>();
        professor.setPromptText("Professor responsável");
        configurarComboUsuario(professor);
        carregarProfessores(professor);

        ListView<Usuario> alunos = new ListView<>();
        alunos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        alunos.setPrefHeight(140);
        alunos.setStyle("-fx-background-color: " + FxTheme.CARD + "; -fx-control-inner-background: "
                + FxTheme.CARD + "; -fx-border-color: " + FxTheme.BORDA + "; -fx-border-radius: 8;");
        alunos.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Usuario item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatarUsuario(item));
                setStyle("-fx-background-color: " + FxTheme.CARD + "; -fx-text-fill: " + FxTheme.TEXTO + "; -fx-padding: 8;");
            }
        });
        carregarAlunos(alunos);

        Button criar = FxTheme.primaryButton("Criar grupo");
        criar.setMaxWidth(Double.MAX_VALUE);
        criar.setOnAction(e -> {
            try {
                GrupoAcademico grupo = new GrupoAcademico();
                grupo.setNome(nome.getText());
                grupo.setTipo(tipo.getValue());
                if ("GRUPO_CURSO".equals(tipo.getValue()) && cursos.getValue() != null) {
                    grupo.setIdCurso(cursos.getValue().getIdCurso());
                }
                if ("GRUPO_MATERIA".equals(tipo.getValue()) && disciplinas.getValue() != null) {
                    grupo.setIdDisciplina(disciplinas.getValue().getIdDisciplina());
                }
                if (professor.getValue() != null) {
                    grupo.setIdProfessorResponsavel(professor.getValue().getIdUsuario());
                }
                grupo.setIdsAlunos(idsAlunos(alunos.getSelectionModel().getSelectedItems()));
                chatService.criarGrupo(grupo, usuarioLogado);
                FxDialogs.info("Grupo criado.");
                carregarConversas();
            } catch (Exception ex) {
                FxDialogs.erro(ex.getMessage());
            }
        });
        box.getChildren().addAll(nome, tipo, disciplinas, cursos, professor, FxTheme.muted("Selecione os alunos iniciais:"), alunos, criar);
        return box;
    }

    private VBox criarGestaoMembros() {
        VBox box = new VBox(8);
        box.getChildren().add(FxTheme.title("Gerenciar grupo", 15));
        ComboBox<Usuario> aluno = new ComboBox<>();
        aluno.setPromptText("Aluno");
        configurarComboUsuario(aluno);
        carregarAlunos(aluno);

        HBox botoes = new HBox(8);
        Button adicionar = FxTheme.secondaryButton("Adicionar");
        adicionar.setOnAction(e -> executarComConversa(() -> {
            if (aluno.getValue() == null) {
                throw new IllegalArgumentException("Selecione um aluno.");
            }
            chatService.adicionarAluno(conversaAtual.getIdConversa(), aluno.getValue().getIdUsuario(), usuarioLogado);
        }));
        Button remover = FxTheme.secondaryButton("Remover");
        remover.setOnAction(e -> executarComConversa(() -> {
            if (aluno.getValue() == null) {
                throw new IllegalArgumentException("Selecione um aluno.");
            }
            chatService.removerAluno(conversaAtual.getIdConversa(), aluno.getValue().getIdUsuario(), usuarioLogado);
        }));
        botoes.getChildren().addAll(adicionar, remover);

        Button excluirGrupo = FxTheme.secondaryButton("Excluir grupo");
        excluirGrupo.setStyle(excluirGrupo.getStyle() + "-fx-text-fill: " + FxTheme.ALERTA + ";");
        excluirGrupo.setMaxWidth(Double.MAX_VALUE);
        excluirGrupo.setOnAction(e -> executarComConversa(() -> {
            if (FxDialogs.confirmar("Excluir este grupo? Ele será arquivado e sairá das listagens.")) {
                chatService.arquivarGrupo(conversaAtual.getIdConversa(), usuarioLogado, "Excluído pela interface.");
            }
        }));
        box.getChildren().addAll(aluno, botoes, excluirGrupo);
        return box;
    }


    private void atualizarTela() {
        Integer idSelecionado = conversaAtual == null ? null : conversaAtual.getIdConversa();
        conversaAtual = null;
        carregarConversas();
        Conversa selecionada = null;
        if (idSelecionado != null) {
            selecionada = conversas.stream()
                    .filter(item -> item.getIdConversa().equals(idSelecionado))
                    .findFirst()
                    .orElse(null);
        }
        if (selecionada == null && !conversas.isEmpty()) {
            selecionada = conversas.get(0);
        }
        listaConversas.getSelectionModel().select(selecionada);
        selecionarConversa(selecionada);
        notificarMudanca();
    }

    private void carregarConversas() {
        try {
            conversas.setAll(chatService.listarConversas(usuarioLogado));
            if (!conversas.isEmpty() && conversaAtual == null) {
                listaConversas.getSelectionModel().selectFirst();
            }
        } catch (Exception ex) {
            FxDialogs.erro(ex.getMessage());
        }
    }

    private void selecionarConversa(Conversa conversa) {
        conversaAtual = conversa;
        mensagensBox.getChildren().clear();
        participantes.getItems().clear();
        if (conversa == null) {
            tituloConversa.setText("Selecione uma conversa");
            detalhesConversa.setText("");
            mensagensBox.getChildren().addAll(FxAssets.view(FxAssets.EMPTY_CHAT, 320, 190), FxTheme.muted("Nenhuma conversa selecionada."));
            return;
        }

        tituloConversa.setText(conversa.getNome());
        detalhesConversa.setText(tipoConversa(conversa));
        try {
            participantes.getItems().setAll(chatService.listarParticipantes(conversa.getIdConversa(), usuarioLogado));
            List<ConversaMensagem> mensagens = chatService.listarMensagens(conversa.getIdConversa(), usuarioLogado);
            if (mensagens.isEmpty()) {
                mensagensBox.getChildren().addAll(FxAssets.view(FxAssets.EMPTY_CHAT, 320, 190), FxTheme.muted("Ainda não há mensagens nesta conversa."));
            }
            for (ConversaMensagem mensagem : mensagens) {
                mensagensBox.getChildren().add(criarMensagem(mensagem));
            }
        } catch (Exception ex) {
            mensagensBox.getChildren().add(FxTheme.muted("Não foi possível carregar esta conversa."));
        }
    }

    private VBox criarMensagem(ConversaMensagem mensagem) {
        boolean minha = usuarioLogado.getIdUsuario().equals(mensagem.getIdAutor());
        VBox bubble = new VBox(4);
        bubble.setMaxWidth(620);
        bubble.setPadding(new Insets(10));
        bubble.setStyle("-fx-background-color: " + (minha ? "#123859" : FxTheme.CARD) + "; "
                + "-fx-border-color: " + FxTheme.BORDA + "; -fx-border-radius: 8; -fx-background-radius: 8;");
        Label autor = new Label(formatarAutorMensagem(mensagem));
        autor.setStyle("-fx-font-weight: 800; -fx-text-fill: " + FxTheme.TEXTO + ";");
        Label texto = new Label(mensagem.getMensagem());
        texto.setWrapText(true);
        texto.setStyle("-fx-text-fill: " + FxTheme.TEXTO + "; -fx-font-size: 13px;");
        Label data = FxTheme.muted(mensagem.getCreatedAt() == null ? "" : mensagem.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        bubble.getChildren().addAll(autor, texto, data);
        HBox wrapper = new HBox(bubble);
        wrapper.setAlignment(minha ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        return new VBox(wrapper);
    }

    private void enviarMensagem() {
        executarComConversa(() -> {
            chatService.enviarMensagem(conversaAtual.getIdConversa(), mensagemField.getText(), null, usuarioLogado);
            mensagemField.clear();
        });
    }

    private void executarComConversa(Acao acao) {
        if (conversaAtual == null) {
            FxDialogs.erro("Selecione uma conversa primeiro.");
            return;
        }
        try {
            Integer idSelecionado = conversaAtual.getIdConversa();
            acao.executar();
            carregarConversas();
            Conversa proxima = conversas.stream()
                    .filter(item -> item.getIdConversa().equals(idSelecionado))
                    .findFirst()
                    .orElse(conversas.isEmpty() ? null : conversas.get(0));
            listaConversas.getSelectionModel().select(proxima);
            selecionarConversa(proxima);
            notificarMudanca();
        } catch (Exception ex) {
            FxDialogs.erro(ex.getMessage());
        }
    }

    private void carregarCursos(ComboBox<Curso> combo) {
        try {
            combo.getItems().setAll(cursoService.listarCursos(usuarioLogado));
        } catch (Exception ex) {
            combo.setPromptText("Cursos indisponíveis");
        }
    }

    private void carregarDisciplinas(ComboBox<Disciplina> combo) {
        try {
            combo.getItems().setAll(disciplinaService.listarCatalogo(usuarioLogado));
        } catch (Exception ex) {
            combo.setPromptText("Matérias indisponíveis");
        }
    }

    private void carregarProfessores(ComboBox<Usuario> combo) {
        try {
            combo.getItems().setAll(chatService.listarProfessoresAtivos(usuarioLogado));
        } catch (Exception ex) {
            combo.setPromptText("Professores indisponíveis");
        }
    }

    private void carregarAlunos(ComboBox<Usuario> combo) {
        try {
            combo.getItems().setAll(chatService.listarAlunosAtivos(usuarioLogado));
        } catch (Exception ex) {
            combo.setPromptText("Alunos indisponíveis");
        }
    }

    private void carregarAlunos(ListView<Usuario> lista) {
        try {
            lista.getItems().setAll(chatService.listarAlunosAtivos(usuarioLogado));
        } catch (Exception ignored) {
        }
    }

    private void configurarComboUsuario(ComboBox<Usuario> combo) {
        combo.setStyle(FxTheme.inputStyle());
        combo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Usuario item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatarUsuario(item));
                setStyle("-fx-background-color: " + FxTheme.CARD + "; -fx-text-fill: " + FxTheme.TEXTO + ";");
            }
        });
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Usuario item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatarUsuario(item));
                setStyle("-fx-text-fill: " + FxTheme.TEXTO + ";");
            }
        });
    }

    private List<Integer> idsAlunos(List<Usuario> alunos) {
        List<Integer> ids = new ArrayList<>();
        for (Usuario aluno : alunos) {
            ids.add(aluno.getIdUsuario());
        }
        return ids;
    }

    private String tipoConversa(Conversa conversa) {
        if ("PRIVADA".equals(conversa.getTipo())) {
            return "Privado";
        }
        if ("GRUPO_CURSO".equals(conversa.getTipo())) {
            return "Grupo de curso" + detalhe(conversa.getNomeCurso());
        }
        return "Grupo de matéria" + detalhe(conversa.getNomeDisciplina());
    }

    private String detalhe(String valor) {
        return valor == null || valor.isBlank() ? "" : " - " + valor;
    }

    private String formatarUsuario(Usuario usuario) {
        return usuario.getNome() + " - " + usuario.getTipoPerfil();
    }

    private String formatarAutorMensagem(ConversaMensagem mensagem) {
        return valor(mensagem.getNomeAutor(), "Usuário") + " - " + valor(mensagem.getPerfilAutor(), "");
    }

    private String valor(String valor, String fallback) {
        return valor == null || valor.isBlank() ? fallback : valor;
    }

    private void notificarMudanca() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }

    private interface Acao {
        void executar();
    }
}
