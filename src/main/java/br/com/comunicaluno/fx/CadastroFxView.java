package br.com.comunicaluno.fx;

import br.com.comunicaluno.model.Curso;
import br.com.comunicaluno.model.Usuario;
import br.com.comunicaluno.service.CursoService;
import br.com.comunicaluno.service.UsuarioService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class CadastroFxView extends BorderPane {

    private final ComunicAlunoFxApp app;
    private final UsuarioService usuarioService;
    private final CursoService cursoService;
    private TextField nomeField;
    private TextField emailField;
    private PasswordField senhaField;
    private ComboBox<String> cursoCombo;
    private TextField turmaField;
    private ComboBox<String> perfilCombo;

    public CadastroFxView(ComunicAlunoFxApp app) {
        this.app = app;
        this.usuarioService = new UsuarioService();
        this.cursoService = new CursoService();
        setStyle("-fx-background-color: " + FxTheme.FUNDO + ";");
        setCenter(criarCard());
    }

    private VBox criarCard() {
        VBox card = FxTheme.card(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(28));
        card.setMaxWidth(460);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        Label logo = new Label("", FxAssets.view(FxAssets.LOGO_MARK, 58, 58));
        Label titulo = FxTheme.title("Criar conta", 25);
        Label aviso = FxTheme.muted("Sua conta nasce como pendente e precisa ser aprovada por admin ou coordenação.");
        aviso.setWrapText(true);

        nomeField = campo("Nome completo");
        emailField = campo("E-mail institucional");
        senhaField = new PasswordField();
        senhaField.setPromptText("Senha com letras e números, mínimo 6 caracteres");
        senhaField.setPrefHeight(42);
        senhaField.setStyle(FxTheme.inputStyle());

        perfilCombo = new ComboBox<>();
        perfilCombo.getItems().addAll("ALUNO", "PROF", "COORDENADOR");
        perfilCombo.setValue("ALUNO");
        perfilCombo.setMaxWidth(Double.MAX_VALUE);
        perfilCombo.setPrefHeight(42);
        perfilCombo.setStyle(FxTheme.inputStyle());

        cursoCombo = new ComboBox<>();
        cursoCombo.setEditable(true);
        cursoCombo.setPromptText("Curso");
        cursoCombo.setMaxWidth(Double.MAX_VALUE);
        cursoCombo.setPrefHeight(42);
        cursoCombo.setStyle(FxTheme.inputStyle());
        carregarCursos();
        turmaField = campo("Turma");

        Button criar = FxTheme.primaryButton("Solicitar cadastro");
        criar.setMaxWidth(Double.MAX_VALUE);
        criar.setOnAction(e -> cadastrar());

        Hyperlink voltar = new Hyperlink("Voltar para login");
        voltar.setOnAction(e -> app.mostrarLogin());

        card.getChildren().addAll(logo, titulo, aviso, nomeField, emailField, senhaField, perfilCombo,
                cursoCombo, turmaField, criar, voltar);
        return card;
    }

    private TextField campo(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(42);
        field.setStyle(FxTheme.inputStyle());
        return field;
    }

    private void cadastrar() {
        try {
            if (!usuarioService.emailValido(emailField.getText())) {
                FxDialogs.erro("Informe um e-mail válido, como nome@dominio.com.");
                return;
            }
            if (!usuarioService.senhaCadastroValida(senhaField.getText())) {
                FxDialogs.erro("A senha deve ter pelo menos 6 caracteres, com letras e números.");
                return;
            }
            Usuario usuario = new Usuario();
            usuario.setNome(nomeField.getText());
            usuario.setEmail(emailField.getText());
            usuario.setSenhaHash(senhaField.getText());
            usuario.setTipoPerfil(perfilCombo.getValue());
            usuario.setCurso(cursoCombo.getEditor().getText());
            usuario.setTurma(turmaField.getText());
            usuarioService.registarNovoUsuario(usuario);
            FxDialogs.info("Cadastro enviado. Aguarde aprovação para acessar.");
            app.mostrarLogin();
        } catch (Exception ex) {
            FxDialogs.erro(ex.getMessage());
        }
    }

    private void carregarCursos() {
        try {
            for (Curso curso : cursoService.listarCursosPublicos()) {
                cursoCombo.getItems().add(curso.getNome());
            }
        } catch (Exception ignored) {
        }
    }
}
