package br.com.comunicaluno.fx;

import br.com.comunicaluno.model.Usuario;
import br.com.comunicaluno.service.UsuarioService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginFxView extends BorderPane {

    private final ComunicAlunoFxApp app;
    private final UsuarioService usuarioService;
    private TextField emailField;
    private PasswordField senhaField;

    public LoginFxView(ComunicAlunoFxApp app) {
        this.app = app;
        this.usuarioService = new UsuarioService();
        setStyle("-fx-background-color: " + FxTheme.FUNDO + ";");
        setCenter(criarConteudo());
    }

    private HBox criarConteudo() {
        HBox root = new HBox(48);
        root.setPadding(new Insets(48, 72, 48, 72));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: " + FxTheme.FUNDO + ";");

        VBox marca = new VBox(18);
        marca.setAlignment(Pos.CENTER_LEFT);
        marca.setMaxWidth(470);
        marca.getChildren().add(FxAssets.view(FxAssets.LOGO_FULL, 390, 110));
        Label titulo = FxTheme.title("Chamados acadêmicos, suporte e feed em um só lugar.", 34);
        titulo.setWrapText(true);
        Label texto = FxTheme.muted("Entre para acompanhar avisos importantes, conversar com a equipe escolar e participar do feed acadêmico.");
        texto.setStyle("-fx-text-fill: " + FxTheme.SUAVE + "; -fx-font-size: 15px;");
        texto.setWrapText(true);
        marca.getChildren().addAll(titulo, texto);

        VBox card = FxTheme.card(12);
        card.setPrefWidth(390);
        card.setMaxWidth(390);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        Label entrar = FxTheme.title("Entrar", 24);
        Label dica = FxTheme.muted("Use sua conta aprovada pela coordenação.");

        emailField = new TextField();
        emailField.setPromptText("E-mail");
        emailField.setPrefHeight(42);
        emailField.setStyle(FxTheme.inputStyle());

        senhaField = new PasswordField();
        senhaField.setPromptText("Senha");
        senhaField.setPrefHeight(42);
        senhaField.setStyle(FxTheme.inputStyle());

        Button login = FxTheme.primaryButton("Entrar");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setOnAction(e -> autenticar());
        senhaField.setOnAction(e -> autenticar());

        Hyperlink cadastro = new Hyperlink("Criar nova conta");
        cadastro.setOnAction(e -> app.mostrarCadastro());

        Label seed = FxTheme.muted("Admin inicial: admin@comunicaluno.com.br / admin123");
        seed.setWrapText(true);

        card.getChildren().addAll(entrar, dica, emailField, senhaField, login, cadastro, seed);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        StackPane cardWrap = new StackPane(card);
        root.getChildren().addAll(marca, spacer, cardWrap);
        return root;
    }

    private void autenticar() {
        try {
            if (!usuarioService.emailValido(emailField.getText())) {
                FxDialogs.erro("Informe um e-mail válido.");
                return;
            }
            if (senhaField.getText() == null || senhaField.getText().isBlank()) {
                FxDialogs.erro("Informe a senha.");
                return;
            }
            Usuario usuario = usuarioService.autenticar(emailField.getText(), senhaField.getText());
            app.mostrarPrincipal(usuario);
        } catch (Exception ex) {
            FxDialogs.erro(ex.getMessage());
        }
    }
}
