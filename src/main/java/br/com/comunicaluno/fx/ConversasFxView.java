package br.com.comunicaluno.fx;

import br.com.comunicaluno.model.Chamado;
import br.com.comunicaluno.model.ChamadoMensagem;
import br.com.comunicaluno.model.Usuario;
import br.com.comunicaluno.service.ArquivoService;
import br.com.comunicaluno.service.ChamadoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConversasFxView extends BorderPane {

    private final Usuario usuarioLogado;
    private final Runnable onDataChanged;
    private final ChamadoService chamadoService = new ChamadoService();
    private final ArquivoService arquivoService = new ArquivoService();
    private final ObservableList<Chamado> chamados = FXCollections.observableArrayList();
    private final ListView<Chamado> listaChamados = new ListView<>(chamados);
    private final VBox mensagensBox = new VBox(10);
    private final Label tituloConversa = FxTheme.title("Selecione uma conversa", 18);
    private final TextField mensagemField = new TextField();
    private String anexoSelecionado;
    private Chamado chamadoAtual;

    public ConversasFxView(Usuario usuarioLogado, Runnable onDataChanged) {
        this.usuarioLogado = usuarioLogado;
        this.onDataChanged = onDataChanged;
        setPadding(new Insets(18, 22, 22, 22));
        setStyle("-fx-background-color: " + FxTheme.FUNDO + ";");
        setLeft(criarLista());
        setCenter(criarConversa());
        if ("ALUNO".equals(usuarioLogado.getTipoPerfil())) {
            setBottom(criarAberturaChamado());
        }
        carregarChamados();
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
        header.getChildren().addAll(FxTheme.title("Chamados", 18), spacer, atualizar);
        box.getChildren().add(header);
        listaChamados.setPrefHeight(520);
        listaChamados.setStyle("-fx-background-color: " + FxTheme.CARD + "; -fx-control-inner-background: "
                + FxTheme.CARD + "; -fx-border-color: " + FxTheme.BORDA + "; -fx-border-radius: 8;");
        listaChamados.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Chamado chamado, boolean empty) {
                super.updateItem(chamado, empty);
                if (empty || chamado == null) {
                    setText(null);
                    setStyle("-fx-background-color: " + FxTheme.CARD + ";");
                } else {
                    setText("#" + chamado.getIdChamado() + " - " + chamado.getAssunto()
                            + "\n" + chamado.getStatus() + " - " + valor(chamado.getNomeAluno(), "Aluno"));
                    setStyle("-fx-background-color: " + FxTheme.CARD + "; -fx-text-fill: " + FxTheme.TEXTO + "; "
                            + "-fx-padding: 10; -fx-border-color: transparent transparent " + FxTheme.BORDA + " transparent;");
                }
            }
        });
        listaChamados.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> selecionarChamado(newValue));
        box.getChildren().add(listaChamados);
        return box;
    }

    private BorderPane criarConversa() {
        BorderPane conversa = new BorderPane();
        conversa.setPadding(new Insets(0, 0, 0, 16));

        VBox topo = FxTheme.card(8);
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button atualizar = FxTheme.secondaryButton("Atualizar página");
        atualizar.setOnAction(e -> atualizarTela());
        header.getChildren().addAll(tituloConversa, spacer, atualizar);
        topo.getChildren().add(header);
        topo.getChildren().add(criarControlesChamado());
        conversa.setTop(topo);

        mensagensBox.setPadding(new Insets(16, 0, 16, 0));
        ScrollPane scroll = new ScrollPane(mensagensBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        conversa.setCenter(scroll);

        HBox envio = new HBox(8);
        envio.setPadding(new Insets(12, 0, 0, 0));
        mensagemField.setPromptText("Digite sua mensagem...");
        mensagemField.setStyle(FxTheme.inputStyle());
        HBox.setHgrow(mensagemField, Priority.ALWAYS);
        Button anexar = FxTheme.secondaryButton("Anexar");
        anexar.setGraphic(FxAssets.view(FxAssets.ICON_ATTACHMENT, 18, 18));
        anexar.setOnAction(e -> selecionarAnexo());
        Button enviar = FxTheme.primaryButton("Enviar");
        enviar.setOnAction(e -> enviarMensagem());
        envio.getChildren().addAll(mensagemField, anexar, enviar);
        conversa.setBottom(envio);
        return conversa;
    }

    private HBox criarControlesChamado() {
        HBox controles = new HBox(8);
        controles.setAlignment(Pos.CENTER_LEFT);
        if (!"ALUNO".equals(usuarioLogado.getTipoPerfil())) {
            Button assumir = FxTheme.secondaryButton("Assumir");
            assumir.setOnAction(e -> executarComChamado(() -> chamadoService.assumirChamado(chamadoAtual.getIdChamado(), usuarioLogado)));

            ComboBox<String> status = new ComboBox<>();
            status.getItems().addAll("ABERTO", "EM_ANALISE", "RESOLVIDO", "FECHADO");
            status.setValue("EM_ANALISE");
            status.setStyle(FxTheme.inputStyle());

            Button atualizar = FxTheme.secondaryButton("Atualizar status");
            atualizar.setOnAction(e -> executarComChamado(() -> chamadoService.alterarStatus(chamadoAtual.getIdChamado(), status.getValue(), usuarioLogado)));
            controles.getChildren().addAll(assumir, status, atualizar);
        }
        Button excluir = FxTheme.secondaryButton("Excluir chamado");
        excluir.setStyle(excluir.getStyle() + "-fx-text-fill: " + FxTheme.ALERTA + ";");
        excluir.setOnAction(e -> executarComChamado(() -> {
            if (FxDialogs.confirmar("Excluir este chamado? Ele será arquivado e sairá das listagens.")) {
                chamadoService.arquivarChamado(chamadoAtual.getIdChamado(), usuarioLogado, "Excluído pela interface.");
            }
        }));
        controles.getChildren().add(excluir);
        return controles;
    }

    private VBox criarAberturaChamado() {
        VBox card = FxTheme.card(8);
        card.setPadding(new Insets(14));
        Label titulo = FxTheme.title("Novo chamado", 16);
        TextField assunto = new TextField();
        assunto.setPromptText("Assunto");
        assunto.setStyle(FxTheme.inputStyle());
        TextArea descricao = new TextArea();
        descricao.setPromptText("Descreva sua solicitação");
        descricao.setPrefRowCount(2);
        descricao.setWrapText(true);
        descricao.setStyle(FxTheme.inputStyle());
        Button abrir = FxTheme.primaryButton("Abrir chamado");
        abrir.setOnAction(e -> {
            try {
                Chamado chamado = new Chamado(assunto.getText(), descricao.getText());
                chamadoService.abrirChamado(chamado, usuarioLogado);
                assunto.clear();
                descricao.clear();
                carregarChamados();
                notificarMudanca();
            } catch (Exception ex) {
                FxDialogs.erro(ex.getMessage());
            }
        });
        card.getChildren().addAll(titulo, assunto, descricao, abrir);
        return card;
    }


    private void atualizarTela() {
        Integer idSelecionado = chamadoAtual == null ? null : chamadoAtual.getIdChamado();
        chamadoAtual = null;
        carregarChamados();
        Chamado selecionado = null;
        if (idSelecionado != null) {
            selecionado = chamados.stream()
                    .filter(item -> item.getIdChamado().equals(idSelecionado))
                    .findFirst()
                    .orElse(null);
        }
        if (selecionado == null && !chamados.isEmpty()) {
            selecionado = chamados.get(0);
        }
        listaChamados.getSelectionModel().select(selecionado);
        selecionarChamado(selecionado);
        notificarMudanca();
    }

    private void carregarChamados() {
        try {
            chamados.setAll(chamadoService.listarChamados(usuarioLogado));
            if (!chamados.isEmpty() && chamadoAtual == null) {
                listaChamados.getSelectionModel().selectFirst();
            }
        } catch (Exception ex) {
            FxDialogs.erro(ex.getMessage());
        }
    }

    private void selecionarChamado(Chamado chamado) {
        chamadoAtual = chamado;
        mensagensBox.getChildren().clear();
        if (chamado == null) {
            tituloConversa.setText("Selecione uma conversa");
            mensagensBox.getChildren().addAll(FxAssets.view(FxAssets.EMPTY_CHAT, 320, 190), FxTheme.muted("Nenhuma conversa selecionada."));
            return;
        }

        tituloConversa.setText("#" + chamado.getIdChamado() + " - " + chamado.getAssunto()
                + " - " + chamado.getStatus());
        try {
            List<ChamadoMensagem> mensagens = chamadoService.listarMensagens(chamado.getIdChamado(), usuarioLogado);
            if (mensagens.isEmpty()) {
                mensagensBox.getChildren().addAll(FxAssets.view(FxAssets.EMPTY_CHAT, 320, 190), FxTheme.muted("Ainda não há mensagens neste chamado."));
            }
            for (ChamadoMensagem mensagem : mensagens) {
                mensagensBox.getChildren().add(criarMensagem(mensagem));
            }
        } catch (Exception ex) {
            mensagensBox.getChildren().add(FxTheme.muted("Não foi possível carregar mensagens."));
        }
    }

    private VBox criarMensagem(ChamadoMensagem mensagem) {
        boolean minha = usuarioLogado.getIdUsuario().equals(mensagem.getIdAutor());
        VBox bubble = new VBox(4);
        bubble.setMaxWidth(620);
        bubble.setPadding(new Insets(10));
        bubble.setStyle("-fx-background-color: " + (minha ? "#123859" : FxTheme.CARD) + "; "
                + "-fx-border-color: " + FxTheme.BORDA + "; -fx-border-radius: 8; -fx-background-radius: 8;");
        Label autor = new Label(valor(mensagem.getNomeAutor(), "Usuário") + " - " + valor(mensagem.getPerfilAutor(), ""));
        autor.setStyle("-fx-font-weight: 800; -fx-text-fill: " + FxTheme.TEXTO + ";");
        Label texto = new Label(mensagem.getMensagem());
        texto.setWrapText(true);
        texto.setStyle("-fx-text-fill: " + FxTheme.TEXTO + "; -fx-font-size: 13px;");
        Label data = FxTheme.muted(mensagem.getCreatedAt() == null ? "" : mensagem.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        bubble.getChildren().addAll(autor, texto, data);
        if (mensagem.getAnexoPath() != null) {
            Button abrir = FxTheme.secondaryButton("Abrir anexo");
            abrir.setOnAction(e -> abrirArquivo(mensagem.getAnexoPath()));
            bubble.getChildren().add(abrir);
        }
        HBox wrapper = new HBox(bubble);
        wrapper.setAlignment(minha ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        VBox container = new VBox(wrapper);
        return container;
    }

    private void selecionarAnexo() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecionar anexo do chamado");
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            anexoSelecionado = arquivoService.salvarUpload(file, "chamados");
            mensagemField.setPromptText("Anexo selecionado: " + file.getName());
        }
    }

    private void enviarMensagem() {
        executarComChamado(() -> {
            chamadoService.enviarMensagem(chamadoAtual.getIdChamado(), mensagemField.getText(), anexoSelecionado, usuarioLogado);
            mensagemField.clear();
            anexoSelecionado = null;
            mensagemField.setPromptText("Digite sua mensagem...");
        });
    }

    private void executarComChamado(Acao acao) {
        if (chamadoAtual == null) {
            FxDialogs.erro("Selecione um chamado primeiro.");
            return;
        }
        try {
            Integer idSelecionado = chamadoAtual.getIdChamado();
            acao.executar();
            carregarChamados();
            Chamado proximo = chamados.stream()
                    .filter(item -> item.getIdChamado().equals(idSelecionado))
                    .findFirst()
                    .orElse(chamados.isEmpty() ? null : chamados.get(0));
            listaChamados.getSelectionModel().select(proximo);
            selecionarChamado(proximo);
            notificarMudanca();
        } catch (Exception ex) {
            FxDialogs.erro(ex.getMessage());
        }
    }

    private void abrirArquivo(String path) {
        try {
            Desktop.getDesktop().open(new File(path));
        } catch (Exception ex) {
            FxDialogs.erro("Não foi possível abrir o anexo.");
        }
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
