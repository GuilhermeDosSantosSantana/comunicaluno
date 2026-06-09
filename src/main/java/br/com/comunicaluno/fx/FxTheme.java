package br.com.comunicaluno.fx;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public final class FxTheme {

    public static final String FUNDO = "#15202B";
    public static final String CARD = "#192734";
    public static final String TEXTO = "#FFFFFF";
    public static final String SUAVE = "#8B98A5";
    public static final String BORDA = "#38444D";
    public static final String AZUL = "#1D9BF0";
    public static final String AZUL_ESCURO = "#0F6EAF";
    public static final String VERDE = "#2DD4BF";
    public static final String ALERTA = "#ef4444";

    private FxTheme() {
    }

    public static VBox card(double spacing) {
        VBox card = new VBox(spacing);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color: " + CARD + "; -fx-background-radius: 10; "
                + "-fx-border-color: " + BORDA + "; -fx-border-radius: 8;");
        return card;
    }

    public static Label title(String text, int size) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + TEXTO + "; -fx-font-size: " + size + "px; -fx-font-weight: 800;");
        return label;
    }

    public static Label muted(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + SUAVE + "; -fx-font-size: 12px;");
        return label;
    }

    public static Button primaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + AZUL + "; -fx-text-fill: white; -fx-font-weight: 700; "
                + "-fx-background-radius: 8; -fx-padding: 9 16;");
        return button;
    }

    public static Button secondaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #22303C; -fx-text-fill: #E7EDF2; "
                + "-fx-border-color: #536471; -fx-border-radius: 8; -fx-background-radius: 8; "
                + "-fx-font-weight: 700; -fx-padding: 8 12;");
        return button;
    }

    public static Button iconMenuButton(String text, Node graphic) {
        Button button = new Button(text, graphic);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setGraphicTextGap(14);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXTO + "; "
                + "-fx-font-size: 15px; -fx-font-weight: 700; -fx-alignment: center-left; "
                + "-fx-padding: 12 14; -fx-background-radius: 6;");
        return button;
    }

    public static void selected(Button button, boolean selected) {
        if (selected) {
            button.setStyle("-fx-background-color: " + AZUL + "; -fx-text-fill: white; "
                    + "-fx-font-size: 15px; -fx-font-weight: 800; -fx-alignment: center-left; "
                    + "-fx-padding: 12 14; -fx-background-radius: 6;");
        } else {
            button.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXTO + "; "
                    + "-fx-font-size: 15px; -fx-font-weight: 700; -fx-alignment: center-left; "
                    + "-fx-padding: 12 14; -fx-background-radius: 6;");
        }
    }

    public static String inputStyle() {
        return "-fx-control-inner-background: " + CARD + "; -fx-background-color: " + CARD + "; "
                + "-fx-text-fill: " + TEXTO + "; -fx-prompt-text-fill: " + SUAVE + "; "
                + "-fx-border-color: " + BORDA + "; -fx-border-radius: 8; -fx-background-radius: 8; "
                + "-fx-background-insets: 0; -fx-font-size: 13px;";
    }

    public static String dividerBottom() {
        return "-fx-border-color: transparent transparent " + BORDA + " transparent;";
    }
}
