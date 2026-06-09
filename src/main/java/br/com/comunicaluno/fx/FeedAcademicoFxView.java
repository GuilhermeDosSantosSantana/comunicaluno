package br.com.comunicaluno.fx;

import br.com.comunicaluno.model.Evento;
import br.com.comunicaluno.model.Post;
import br.com.comunicaluno.model.PostComentario;
import br.com.comunicaluno.model.Usuario;
import br.com.comunicaluno.service.ArquivoService;
import br.com.comunicaluno.service.EventoService;
import br.com.comunicaluno.service.PostService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;

public class FeedAcademicoFxView extends StackPane {

    private static final int LIMITE_POST = 500;

    private final Usuario usuarioLogado;
    private final Supplier<String> buscaSupplier;
    private final Runnable onDataChanged;
    private final PostService postService = new PostService();
    private final EventoService eventoService = new EventoService();
    private final ArquivoService arquivoService = new ArquivoService();
    private final VBox feedList = new VBox(0);

    private String imagemPathSelecionada;
    private String anexoPathSelecionado;

    public FeedAcademicoFxView(Usuario usuarioLogado, Supplier<String> buscaSupplier, Runnable onDataChanged) {
        this.usuarioLogado = usuarioLogado;
        this.buscaSupplier = buscaSupplier;
        this.onDataChanged = onDataChanged;
        setStyle("-fx-background-color: " + FxTheme.FUNDO + ";");
        getChildren().add(criarConteudo());
        carregarFeed();
    }

    public void abrirModalNovaPublicacao() {
        imagemPathSelecionada = null;
        anexoPathSelecionado = null;
        TextArea texto = modalTextArea("No que você está pensando?");
        Label contador = contador(texto);
        Label anexo = FxTheme.muted("Nenhuma mídia selecionada.");

        VBox modal = modalBase("Nova publicação");
        HBox autor = autorModal();
        HBox ferramentas = ferramentasModal(anexo);
        Button publicar = FxTheme.primaryButton("Publicar");
        publicar.setOnAction(e -> {
            try {
                Post post = new Post();
                post.setTexto(texto.getText());
                post.setTipoPost("PUBLICACAO");
                post.setPublicoAlvo("TODOS");
                post.setImagemPath(imagemPathSelecionada);
                post.setAnexoPath(anexoPathSelecionado);
                postService.publicar(post, usuarioLogado);
                fecharModal();
                carregarFeed();
                notificarMudanca();
            } catch (Exception ex) {
                FxDialogs.erro(ex.getMessage());
            }
        });

        HBox footer = footerModal(ferramentas, contador, publicar);
        modal.getChildren().addAll(autor, texto, anexo, footer);
        abrirOverlay(modal);
    }

    private HBox criarConteudo() {
        HBox layout = new HBox(0);
        layout.setStyle("-fx-background-color: " + FxTheme.FUNDO + ";");

        VBox centro = new VBox(0);
        HBox.setHgrow(centro, Priority.ALWAYS);
        centro.setMinWidth(520);
        centro.setStyle("-fx-border-color: transparent " + FxTheme.BORDA + " transparent transparent;");
        centro.getChildren().add(criarTituloFeed());
        centro.getChildren().add(criarScrollFeed());

        VBox direita = criarColunaEventos();
        direita.setPrefWidth(360);
        direita.setMinWidth(320);

        layout.getChildren().addAll(centro, direita);
        return layout;
    }

    private HBox criarTituloFeed() {
        HBox header = new HBox(10);
        header.setPadding(new Insets(16, 22, 16, 22));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(FxTheme.dividerBottom());
        Label title = FxTheme.title("Feed acadêmico", 18);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button atualizar = FxTheme.secondaryButton("Atualizar página");
        atualizar.setOnAction(e -> {
            carregarFeed();
            notificarMudanca();
        });
        header.getChildren().addAll(title, spacer, atualizar);
        return header;
    }

    private ScrollPane criarScrollFeed() {
        feedList.setFillWidth(true);
        ScrollPane scroll = new ScrollPane(feedList);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: " + FxTheme.FUNDO + "; -fx-background-color: " + FxTheme.FUNDO + "; "
                + "-fx-control-inner-background: " + FxTheme.FUNDO + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return scroll;
    }

    private VBox criarColunaEventos() {
        VBox direita = new VBox(18);
        direita.setPadding(new Insets(18, 22, 18, 22));
        direita.setStyle("-fx-background-color: " + FxTheme.FUNDO + ";");
        direita.getChildren().add(FxTheme.title("Próximos eventos", 18));
        try {
            List<Evento> eventos = eventoService.listarProximos(usuarioLogado, 6);
            if (eventos.isEmpty()) {
                direita.getChildren().add(FxTheme.muted("Nenhum evento próximo."));
            }
            for (Evento evento : eventos) {
                direita.getChildren().add(eventoLinha(evento));
            }
        } catch (Exception ex) {
            direita.getChildren().add(FxTheme.muted("Eventos indisponíveis agora."));
        }
        return direita;
    }

    private HBox eventoLinha(Evento evento) {
        HBox linha = new HBox(12);
        linha.setAlignment(Pos.TOP_LEFT);
        linha.setPadding(new Insets(8, 0, 8, 0));

        BorderPane icon = new BorderPane(FxAssets.view(FxAssets.ICON_CALENDAR, 38, 38));
        icon.setMinSize(62, 62);
        icon.setMaxSize(62, 62);
        icon.setStyle("-fx-background-color: transparent; -fx-border-color: " + FxTheme.BORDA + "; "
                + "-fx-border-radius: 8; -fx-background-radius: 8;");

        VBox texto = new VBox(3);
        Label titulo = label(evento.getTitulo(), 14, true, FxTheme.TEXTO);
        Label depto = label(valor(evento.getNomeCriador(), "Departamento acadêmico"), 13, false, FxTheme.SUAVE);
        Label data = label(dataEvento(evento), 13, false, FxTheme.SUAVE);
        Label local = label(valor(evento.getLocalEvento(), "Local a definir"), 13, false, FxTheme.SUAVE);
        texto.getChildren().addAll(titulo, depto, data, local);
        linha.getChildren().addAll(icon, texto);
        return linha;
    }

    private void carregarFeed() {
        feedList.getChildren().clear();
        try {
            List<Post> posts = postService.listarFeed(usuarioLogado, "MEU_PUBLICO", buscaSupplier.get());
            if (posts.isEmpty()) {
                feedList.getChildren().add(emptyState());
                return;
            }
            for (Post post : posts) {
                feedList.getChildren().add(postRow(post));
            }
        } catch (Exception ex) {
            VBox erro = new VBox(8);
            erro.setPadding(new Insets(24));
            erro.getChildren().addAll(FxTheme.title("Não foi possível carregar o feed", 16), FxTheme.muted(ex.getMessage()));
            feedList.getChildren().add(erro);
        }
    }

    private VBox emptyState() {
        VBox vazio = new VBox(12);
        vazio.setAlignment(Pos.CENTER);
        vazio.setPadding(new Insets(48, 24, 48, 24));
        vazio.setStyle(FxTheme.dividerBottom());
        Button nova = FxTheme.primaryButton("+ Nova publicação");
        nova.setOnAction(e -> abrirModalNovaPublicacao());
        vazio.getChildren().addAll(FxAssets.view(FxAssets.EMPTY_FEED, 260, 160),
                FxTheme.title("Ainda não há publicações", 18),
                FxTheme.muted("Seja o primeiro a compartilhar algo com a comunidade."), nova);
        return vazio;
    }

    private HBox postRow(Post post) {
        HBox row = new HBox(14);
        row.setPadding(new Insets(16, 22, 14, 22));
        row.setStyle("-fx-background-color: transparent; " + FxTheme.dividerBottom());
        row.setAlignment(Pos.TOP_LEFT);

        Image avatar = FxAssets.imageFromPathOrAsset(post.getAvatarAutorPath(), FxAssets.AVATAR);
        VBox corpo = new VBox(8);
        HBox.setHgrow(corpo, Priority.ALWAYS);

        HBox meta = new HBox(7);
        meta.setAlignment(Pos.CENTER_LEFT);
        Label nome = label(valor(post.getNomeAutor(), "Usuário"), 15, true, FxTheme.TEXTO);
        Label user = label(usuarioHandle(post), 14, false, FxTheme.SUAVE);
        Label tempo = label("· " + tempoRelativo(post.getCreatedAt()), 14, false, FxTheme.SUAVE);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        meta.getChildren().addAll(nome, user, tempo, spacer);
        if (postService.podeArquivar(post, usuarioLogado)) {
            Button excluir = actionButton("Excluir");
            excluir.setStyle(excluir.getStyle() + "-fx-text-fill: " + FxTheme.ALERTA + ";");
            excluir.setOnAction(e -> arquivarPost(post));
            meta.getChildren().add(excluir);
        } else {
            meta.getChildren().add(label("...", 18, true, FxTheme.SUAVE));
        }

        Label conteudo = label(post.getTexto(), 15, false, FxTheme.TEXTO);
        conteudo.setWrapText(true);
        corpo.getChildren().addAll(meta, conteudo);

        Node midia = midia(post);
        if (midia != null) {
            corpo.getChildren().add(midia);
        }
        corpo.getChildren().add(acoes(post));

        row.getChildren().addAll(FxAssets.view(avatar, 56, 56), corpo);
        return row;
    }

    private Node midia(Post post) {
        if (post.getImagemPath() != null && !post.getImagemPath().isBlank()) {
            Image image = FxAssets.imageFromPathOrAsset(post.getImagemPath(), FxAssets.POST_IMAGE);
            BorderPane wrapper = new BorderPane(FxAssets.view(image, 520, 240));
            wrapper.setPadding(new Insets(8));
            wrapper.setStyle("-fx-border-color: " + FxTheme.BORDA + "; -fx-border-radius: 12; -fx-background-radius: 12;");
            wrapper.setOnMouseClicked(e -> abrirArquivo(post.getImagemPath()));
            return wrapper;
        }
        if (post.getAnexoPath() != null && !post.getAnexoPath().isBlank()) {
            HBox anexo = new HBox(10);
            anexo.setAlignment(Pos.CENTER_LEFT);
            anexo.setPadding(new Insets(10));
            anexo.setStyle("-fx-border-color: " + FxTheme.BORDA + "; -fx-border-radius: 8;");
            File file = new File(post.getAnexoPath());
            Button abrir = actionButton("Abrir anexo");
            abrir.setOnAction(e -> abrirArquivo(post.getAnexoPath()));
            anexo.getChildren().addAll(FxAssets.view(FxAssets.ATTACHMENT, 36, 36),
                    label(file.getName(), 13, false, FxTheme.TEXTO), abrir);
            return anexo;
        }
        return null;
    }

    private HBox acoes(Post post) {
        HBox acoes = new HBox(28);
        acoes.setAlignment(Pos.CENTER_LEFT);

        Button abrir = actionButton("Abrir publicação");
        abrir.setOnAction(e -> abrirPublicacao(post));

        Button comentar = actionButton(String.valueOf(post.getTotalComentarios()), FxAssets.ICON_COMMENT, false);
        comentar.setOnAction(e -> abrirPublicacao(post));

        Button curtir = actionButton(String.valueOf(post.getTotalCurtidas()),
                post.isCurtidoPeloUsuario() ? FxAssets.ICON_LIKE_FILLED : FxAssets.ICON_LIKE_OUTLINE,
                post.isCurtidoPeloUsuario());
        curtir.setOnAction(e -> executarEAtualizar(() -> postService.alternarCurtida(post.getIdPost(), usuarioLogado)));

        acoes.getChildren().addAll(abrir, comentar, curtir);
        return acoes;
    }

    private void arquivarPost(Post post) {
        if (!FxDialogs.confirmar("Excluir esta publicação? Ela será arquivada e sairá das listagens.")) {
            return;
        }
        executarEAtualizar(() -> postService.arquivarPost(post.getIdPost(), usuarioLogado, "Excluído pela interface."));
    }

    private void abrirModalResposta(Post post) {
        TextArea resposta = modalTextArea("Escreva sua resposta...");
        Label contador = contador(resposta);
        VBox modal = modalBase("Responder");

        VBox original = new VBox(4);
        original.setPadding(new Insets(6, 0, 10, 0));
        original.setStyle(FxTheme.dividerBottom());
        original.getChildren().addAll(label(valor(post.getNomeAutor(), "Usuário") + "  " + usuarioHandle(post)
                        + " · " + tempoRelativo(post.getCreatedAt()), 12, true, FxTheme.SUAVE),
                label(post.getTexto(), 12, false, FxTheme.TEXTO));

        Button responder = FxTheme.primaryButton("Responder");
        responder.setOnAction(e -> {
            try {
                postService.comentar(post.getIdPost(), resposta.getText(), usuarioLogado);
                fecharModal();
                carregarFeed();
                notificarMudanca();
            } catch (Exception ex) {
                FxDialogs.erro(ex.getMessage());
            }
        });

        modal.getChildren().addAll(original, autorModal(), resposta, footerModal(new HBox(8), contador, responder));
        abrirOverlay(modal);
    }


    private void abrirPublicacao(Post postReferencia) {
        VBox modal = new VBox(12);
        modal.setPrefWidth(760);
        modal.setMaxWidth(760);
        modal.setMaxHeight(720);
        modal.setPadding(new Insets(16, 18, 18, 18));
        modal.setStyle("-fx-background-color: " + FxTheme.CARD + "; -fx-background-radius: 10; "
                + "-fx-border-color: " + FxTheme.BORDA + "; -fx-border-radius: 10;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = label("Post", 18, true, FxTheme.TEXTO);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button atualizar = FxTheme.secondaryButton("Atualizar página");
        Button close = actionButton("X");
        close.setOnAction(e -> fecharModal());
        header.getChildren().addAll(title, spacer, atualizar, close);

        VBox publicacaoBox = new VBox(10);
        VBox comentariosBox = new VBox(8);
        Label resumoComentarios = FxTheme.muted("");

        VBox conteudoScroll = new VBox(14, publicacaoBox, FxTheme.title("Comentários", 16), resumoComentarios, comentariosBox);
        conteudoScroll.setPadding(new Insets(0, 4, 0, 0));
        ScrollPane scroll = new ScrollPane(conteudoScroll);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(450);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        TextArea resposta = modalTextArea("Escreva um comentário...");
        resposta.setPrefRowCount(3);
        resposta.setPrefHeight(95);
        resposta.setMinHeight(95);
        resposta.setMaxHeight(95);
        Label contador = contador(resposta);
        Button comentar = FxTheme.primaryButton("Comentar");

        Runnable[] carregar = new Runnable[1];
        carregar[0] = () -> {
            try {
                Post postAtualizado = postService.buscarPublicacao(postReferencia.getIdPost(), usuarioLogado);
                publicacaoBox.getChildren().setAll(publicacaoDetalhada(postAtualizado));

                List<PostComentario> comentarios = postService.listarComentarios(postReferencia.getIdPost(), usuarioLogado);
                comentariosBox.getChildren().clear();
                if (comentarios.isEmpty()) {
                    resumoComentarios.setText("Ainda não há comentários nesta publicação.");
                } else if (comentarios.size() == 1) {
                    resumoComentarios.setText("1 comentário encontrado.");
                } else {
                    resumoComentarios.setText(comentarios.size() + " comentários encontrados.");
                }
                for (PostComentario comentario : comentarios) {
                    comentariosBox.getChildren().add(comentarioRow(comentario));
                }
            } catch (Exception ex) {
                FxDialogs.erro(ex.getMessage());
            }
        };

        atualizar.setOnAction(e -> carregar[0].run());
        comentar.setOnAction(e -> {
            try {
                postService.comentar(postReferencia.getIdPost(), resposta.getText(), usuarioLogado);
                resposta.clear();
                carregar[0].run();
                carregarFeed();
                notificarMudanca();
            } catch (Exception ex) {
                FxDialogs.erro(ex.getMessage());
            }
        });

        HBox footer = footerModal(new HBox(8), contador, comentar);
        modal.getChildren().addAll(header, scroll, autorModal(), resposta, footer);
        abrirOverlay(modal);
        carregar[0].run();
    }

    private VBox publicacaoDetalhada(Post post) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(0, 0, 14, 0));
        box.setStyle(FxTheme.dividerBottom());

        HBox autor = new HBox(10);
        autor.setAlignment(Pos.CENTER_LEFT);
        VBox meta = new VBox(2);
        meta.getChildren().addAll(
                label(valor(post.getNomeAutor(), "Usuário"), 15, true, FxTheme.TEXTO),
                label(usuarioHandle(post) + " · " + tempoRelativo(post.getCreatedAt()), 13, false, FxTheme.SUAVE)
        );
        autor.getChildren().addAll(FxAssets.view(FxAssets.imageFromPathOrAsset(post.getAvatarAutorPath(), FxAssets.AVATAR), 46, 46), meta);

        Label conteudo = label(post.getTexto(), 16, false, FxTheme.TEXTO);
        conteudo.setWrapText(true);
        box.getChildren().addAll(autor, conteudo);
        Node midia = midia(post);
        if (midia != null) {
            box.getChildren().add(midia);
        }
        box.getChildren().add(FxTheme.muted(post.getTotalCurtidas() + " curtidas  •  "
                + post.getTotalComentarios() + " comentários  •  " + dataHora(post.getCreatedAt())));
        return box;
    }

    private VBox comentarioRow(PostComentario comentario) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #15202B; -fx-border-color: " + FxTheme.BORDA + "; "
                + "-fx-border-radius: 8; -fx-background-radius: 8;");
        card.getChildren().addAll(
                label(valor(comentario.getNomeAutor(), "Usuário") + " · "
                        + valor(comentario.getPerfilAutor(), "") + " · " + tempoRelativo(comentario.getCreatedAt()), 12, true, FxTheme.SUAVE),
                label(comentario.getComentario(), 14, false, FxTheme.TEXTO)
        );
        return card;
    }

    private String dataHora(LocalDateTime data) {
        if (data == null) {
            return "-";
        }
        return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private VBox modalBase(String titulo) {
        VBox modal = new VBox(12);
        modal.setPrefWidth(560);
        modal.setMaxWidth(560);
        modal.setMaxHeight(Region.USE_PREF_SIZE);
        modal.setPadding(new Insets(16, 18, 18, 18));
        modal.setStyle("-fx-background-color: " + FxTheme.CARD + "; -fx-background-radius: 10; "
                + "-fx-border-color: " + FxTheme.BORDA + "; -fx-border-radius: 10;");
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = label(titulo, 14, true, FxTheme.TEXTO);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button close = actionButton("X");
        close.setOnAction(e -> fecharModal());
        header.getChildren().addAll(title, spacer, close);
        modal.getChildren().add(header);
        return modal;
    }

    private HBox autorModal() {
        HBox autor = new HBox(10);
        autor.setAlignment(Pos.CENTER_LEFT);
        VBox textos = new VBox(2);
        textos.getChildren().addAll(label(usuarioLogado.getNome(), 13, true, FxTheme.TEXTO),
                label(handle(usuarioLogado.getNome()), 12, false, FxTheme.SUAVE));
        autor.getChildren().addAll(FxAssets.view(FxAssets.AVATAR, 44, 44), textos);
        return autor;
    }

    private TextArea modalTextArea(String prompt) {
        TextArea area = new TextArea();
        area.setPromptText(prompt);
        area.setWrapText(true);
        area.setPrefRowCount(6);
        area.setPrefHeight(140);
        area.setMinHeight(140);
        area.setMaxHeight(140);
        area.setStyle("-fx-control-inner-background: #15202B; -fx-background-color: #15202B; "
                + "-fx-text-fill: " + FxTheme.TEXTO + "; -fx-prompt-text-fill: " + FxTheme.SUAVE + "; "
                + "-fx-border-color: " + FxTheme.BORDA + "; -fx-border-radius: 8; -fx-background-radius: 8;");
        area.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > LIMITE_POST) {
                area.setText(newValue.substring(0, LIMITE_POST));
            }
        });
        return area;
    }

    private Label contador(TextArea area) {
        Label contador = FxTheme.muted("0/" + LIMITE_POST);
        area.textProperty().addListener((obs, oldValue, newValue) ->
                contador.setText((newValue == null ? 0 : newValue.length()) + "/" + LIMITE_POST));
        return contador;
    }

    private HBox ferramentasModal(Label status) {
        HBox tools = new HBox(12);
        Button imagem = actionButton("Imagem", FxAssets.ICON_IMAGE, false);
        imagem.setOnAction(e -> selecionarImagem(status));
        Button arquivo = actionButton("Anexo", FxAssets.ICON_ATTACHMENT, false);
        arquivo.setOnAction(e -> selecionarArquivo(status));
        tools.getChildren().addAll(imagem, arquivo);
        return tools;
    }

    private HBox footerModal(HBox ferramentas, Label contador, Button primary) {
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        footer.getChildren().addAll(ferramentas, spacer, contador, primary);
        return footer;
    }

    private void abrirOverlay(Node modal) {
        StackPane overlay = new StackPane(modal);
        overlay.setPadding(new Insets(24));
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.60);");
        StackPane.setAlignment(modal, Pos.CENTER);
        overlay.setUserData("modal");
        getChildren().add(overlay);
    }

    private void fecharModal() {
        getChildren().removeIf(node -> "modal".equals(node.getUserData()));
    }

    private void selecionarImagem(Label status) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecionar imagem da publicação");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            imagemPathSelecionada = arquivoService.salvarUpload(file, "posts");
            status.setText("Imagem: " + file.getName());
        }
    }

    private void selecionarArquivo(Label status) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecionar anexo da publicação");
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            anexoPathSelecionado = arquivoService.salvarUpload(file, "posts");
            status.setText("Anexo: " + file.getName());
        }
    }

    private Button actionButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: #E7EDF2; "
                + "-fx-font-size: 14px; -fx-font-weight: 700; -fx-padding: 6 8;");
        return button;
    }

    private Button actionButton(String text, String assetName, boolean active) {
        Button button = actionButton(text);
        button.setGraphic(FxAssets.view(assetName, 24, 24));
        button.setGraphicTextGap(8);
        if (active) {
            button.setStyle(button.getStyle() + "-fx-text-fill: " + FxTheme.AZUL + ";");
        }
        return button;
    }

    private Label label(String text, int size, boolean bold, String color) {
        Label label = new Label(text == null ? "" : text);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: " + size + "px; "
                + (bold ? "-fx-font-weight: 800;" : ""));
        return label;
    }

    private void executarEAtualizar(Acao acao) {
        try {
            acao.executar();
            carregarFeed();
            notificarMudanca();
        } catch (Exception ex) {
            FxDialogs.erro(ex.getMessage());
        }
    }

    private void abrirArquivo(String path) {
        try {
            if (path != null) {
                Desktop.getDesktop().open(new File(path));
            }
        } catch (Exception ex) {
            FxDialogs.erro("Não foi possível abrir o arquivo.");
        }
    }

    private String usuarioHandle(Post post) {
        return handle(valor(post.getNomeAutor(), "usuario"));
    }

    private String handle(String nome) {
        String normalized = Normalizer.normalize(nome == null ? "usuario" : nome, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", ".")
                .replaceAll("^\\.|\\.$", "");
        return "@" + (normalized.isBlank() ? "usuario" : normalized);
    }

    private String tempoRelativo(LocalDateTime data) {
        if (data == null) {
            return "agora";
        }
        Duration duration = Duration.between(data, LocalDateTime.now());
        if (duration.toMinutes() < 1) {
            return "agora";
        }
        if (duration.toMinutes() < 60) {
            return duration.toMinutes() + "min";
        }
        if (duration.toHours() < 24) {
            return duration.toHours() + "h";
        }
        return duration.toDays() + "d";
    }

    private String dataEvento(Evento evento) {
        if (evento.getDataHora() == null) {
            return "Data a definir";
        }
        return evento.getDataHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm"));
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
