package br.com.comunicaluno.fx;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public final class FxDialogs {

    private FxDialogs() {
    }

    public static void erro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    public static void info(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("ComunicAluno");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    public static boolean confirmar(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }
}
