package br.com.comunicaluno.fx;

import br.com.comunicaluno.model.Usuario;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ComunicAlunoFxApp extends Application {

    private Stage stage;

    public static void launchApp(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("ComunicAluno 3.1");
        stage.setMinWidth(1120);
        stage.setMinHeight(720);
        mostrarLogin();
        stage.show();
    }

    public void mostrarLogin() {
        LoginFxView view = new LoginFxView(this);
        setScene(new Scene(view, 1120, 720));
    }

    public void mostrarCadastro() {
        CadastroFxView view = new CadastroFxView(this);
        setScene(new Scene(view, 1120, 720));
    }

    public void mostrarPrincipal(Usuario usuario) {
        MainShellFxView view = new MainShellFxView(this, usuario);
        setScene(new Scene(view, 1280, 780));
        stage.centerOnScreen();
    }

    private void setScene(Scene scene) {
        String style = scene.getRoot().getStyle();
        scene.getRoot().setStyle(style + "; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        stage.setScene(scene);
    }
}
