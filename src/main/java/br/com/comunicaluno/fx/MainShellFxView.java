package br.com.comunicaluno.fx;

import br.com.comunicaluno.model.Usuario;
import br.com.comunicaluno.service.ChamadoService;
import br.com.comunicaluno.service.NotificacaoService;
import br.com.comunicaluno.service.UsuarioService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MainShellFxView extends StackPane {

    private final ComunicAlunoFxApp app;
    private final Usuario usuarioLogado;
    private final ChamadoService chamadoService = new ChamadoService();
    private final NotificacaoService notificacaoService = new NotificacaoService();
    private final UsuarioService usuarioService = new UsuarioService();
    private final BorderPane shell = new BorderPane();
    private final Map<String, Button> menuButtons = new LinkedHashMap<>();
    private final Label tituloTela = FxTheme.title("", 16);
    private FeedAcademicoFxView activeFeed;
    private String telaAtual = "INICIAL";

    public MainShellFxView(ComunicAlunoFxApp app, Usuario usuarioLogado) {
        this.app = app;
        this.usuarioLogado = usuarioLogado;
        setStyle("-fx-background-color: " + FxTheme.FUNDO + ";");
        shell.setStyle("-fx-background-color: " + FxTheme.FUNDO + ";");
        shell.setLeft(criarSidebar());
        shell.setTop(criarTopbar());
        getChildren().add(shell);
        mostrarTela("INICIAL");
    }

    private VBox criarSidebar() {
        VBox sidebar = new VBox(14);
        sidebar.setPrefWidth(230);
        sidebar.setPadding(new Insets(18, 14, 22, 14));
        sidebar.setStyle("-fx-background-color: #0F1A24; -fx-border-color: transparent " + FxTheme.BORDA + " transparent transparent;");

        HBox marca = new HBox(10);
        marca.setAlignment(Pos.CENTER_LEFT);
        marca.getChildren().addAll(FxAssets.view(FxAssets.LOGO_MARK, 46, 46), logoTexto());

        sidebar.getChildren().add(marca);
        adicionarItem(sidebar, "INICIAL", "Inicial", FxAssets.ICON_HOME);
        adicionarItem(sidebar, "AVISOS", "Avisos e Notificações", FxAssets.ICON_ANNOUNCEMENT);
        adicionarItem(sidebar, "EVENTOS", "Eventos", FxAssets.ICON_CALENDAR);
        adicionarItem(sidebar, "ACADEMICO", "Cursos e Disciplinas", FxAssets.ICON_FEED);
        adicionarItem(sidebar, "CHAT", "Chat", FxAssets.ICON_COMMENT);
        adicionarItem(sidebar, "CONVERSAS", "Chamados", FxAssets.ICON_COMMENT);

        if (usuarioService.podeAdministrarContas(usuarioLogado)) {
            adicionarItem(sidebar, "CONTAS", "Contas", FxAssets.ICON_PROFILE);
        }

        adicionarItem(sidebar, "PERFIL", "Perfil", FxAssets.ICON_PROFILE);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox novaPublicacao = new VBox(8);
        novaPublicacao.setAlignment(Pos.CENTER);
        Button plus = new Button("+");
        plus.setMinSize(62, 62);
        plus.setPrefSize(62, 62);
        plus.setMaxSize(62, 62);
        plus.setStyle("-fx-background-color: " + FxTheme.AZUL + "; -fx-text-fill: white; "
                + "-fx-background-radius: 99; -fx-font-size: 42px; -fx-font-weight: 600; "
                + "-fx-padding: 0; -fx-alignment: center;");
        plus.setOnAction(e -> abrirNovaPublicacao());
        Label label = FxTheme.muted("Nova Publicação");
        label.setStyle("-fx-text-fill: " + FxTheme.TEXTO + "; -fx-font-size: 13px; -fx-font-weight: 700;");
        novaPublicacao.getChildren().addAll(plus, label);

        Button sair = FxTheme.iconMenuButton("Sair", FxAssets.view(FxAssets.ICON_EXIT, 28, 28));
        sair.setOnAction(e -> app.mostrarLogin());
        sidebar.getChildren().addAll(spacer, novaPublicacao, sair);
        return sidebar;
    }

    private Label logoTexto() {
        Label texto = new Label("comunica\naluno");
        texto.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 900; -fx-line-spacing: -3;");
        return texto;
    }

    private void adicionarItem(VBox sidebar, String key, String texto, String icone) {
        Button button = FxTheme.iconMenuButton(texto, FxAssets.view(icone, 28, 28));
        button.setOnAction(e -> mostrarTela(key));
        menuButtons.put(key, button);
        sidebar.getChildren().add(button);
    }

    private HBox criarTopbar() {
        HBox topbar = new HBox(14);
        topbar.setPadding(new Insets(14, 20, 14, 20));
        topbar.setAlignment(Pos.CENTER_LEFT);
        topbar.setStyle("-fx-background-color: " + FxTheme.FUNDO + "; " + FxTheme.dividerBottom());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button notificacoes = FxTheme.secondaryButton("Avisos" + badgeNotificacoes());
        notificacoes.setOnAction(e -> mostrarTela("AVISOS"));

        Button conversas = FxTheme.secondaryButton("Chamados" + badgeChamados());
        conversas.setOnAction(e -> mostrarTela("CONVERSAS"));

        VBox perfil = new VBox(1);
        perfil.setAlignment(Pos.CENTER_RIGHT);
        Label nome = new Label(usuarioLogado.getNome());
        nome.setStyle("-fx-text-fill: " + FxTheme.TEXTO + "; -fx-font-weight: 800;");
        Label detalhe = FxTheme.muted(usuarioLogado.getTipoPerfil());
        perfil.getChildren().addAll(nome, detalhe);

        topbar.getChildren().addAll(tituloTela, spacer, notificacoes, conversas, perfil, FxAssets.view(FxAssets.AVATAR, 42, 42));
        return topbar;
    }

    private void mostrarTela(String tela) {
        this.telaAtual = tela;
        menuButtons.forEach((key, button) -> FxTheme.selected(button, key.equals(tela)));
        tituloTela.setText(tituloPara(tela));
        shell.setCenter(criarConteudo(tela));
    }

    private Node criarConteudo(String tela) {
        Supplier<String> busca = () -> "";
        Runnable atualizar = () -> shell.setTop(criarTopbar());
        return switch (tela) {
            case "AVISOS" -> SimpleFxViews.avisosNotificacoes(usuarioLogado, busca, atualizar);
            case "EVENTOS" -> SimpleFxViews.eventos(usuarioLogado, atualizar);
            case "ACADEMICO" -> SimpleFxViews.cursosDisciplinas(usuarioLogado, atualizar);
            case "CHAT" -> new ChatFxView(usuarioLogado, atualizar);
            case "CONVERSAS" -> new ConversasFxView(usuarioLogado, atualizar);
            case "CONTAS" -> SimpleFxViews.contas(usuarioLogado, atualizar);
            case "PERFIL" -> SimpleFxViews.perfilConfiguracoes(usuarioLogado, atualizar);
            default -> {
                activeFeed = new FeedAcademicoFxView(usuarioLogado, busca, atualizar);
                yield activeFeed;
            }
        };
    }

    private void abrirNovaPublicacao() {
        if (!"INICIAL".equals(telaAtual) || activeFeed == null) {
            mostrarTela("INICIAL");
        }
        activeFeed.abrirModalNovaPublicacao();
    }

    private String tituloPara(String tela) {
        return switch (tela) {
            case "AVISOS" -> "Avisos e Notificações";
            case "EVENTOS" -> "Eventos";
            case "ACADEMICO" -> "Cursos e Disciplinas";
            case "CHAT" -> "Chat";
            case "CONVERSAS" -> "Chamados";
            case "CONTAS" -> "Contas";
            case "PERFIL" -> "Perfil e Configurações";
            default -> "Inicial";
        };
    }

    private String badgeNotificacoes() {
        try {
            int total = notificacaoService.contarNaoLidas(usuarioLogado);
            return total > 0 ? " (" + total + ")" : "";
        } catch (Exception ex) {
            return "";
        }
    }

    private String badgeChamados() {
        try {
            int total = chamadoService.contarAbertos(usuarioLogado);
            return total > 0 ? " (" + total + ")" : "";
        } catch (Exception ex) {
            return "";
        }
    }
}
