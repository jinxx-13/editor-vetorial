package org.example.Views;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.example.Controller.EditorContext;

/** Barra de status: coordenadas, total de formas e área/perímetro em tempo real. */
public class PainelStatusView extends HBox {

    private final EditorContext contexto;
    private final Label lblCoordenadas = new Label();
    private final Label lblTotal = new Label();
    private final Label lblMedidas = new Label();
    private final Label lblMensagem = new Label();

    public PainelStatusView(EditorContext contexto) {
        this.contexto = contexto;
        setSpacing(28);
        setPadding(new Insets(4, 10, 4, 10));
        setStyle("-fx-background-color: #e8e8e8; -fx-border-color: #bbbbbb transparent transparent transparent;");
        lblMensagem.setStyle("-fx-text-fill: #a00000;");
        getChildren().addAll(lblCoordenadas, lblTotal, lblMedidas, lblMensagem);
        atualizar();
    }

    public void atualizar() {
        lblCoordenadas.setText(String.format("X: %.0f   Y: %.0f", contexto.getCursorX(), contexto.getCursorY()));
        lblTotal.setText("Formas: " + contexto.getFormas().size()
                + "  |  Selecionadas: " + contexto.getSelecionados().size());
        if (contexto.temSelecao()) {
            lblMedidas.setText(String.format("Área: %.2f   Perímetro: %.2f",
                    contexto.getAreaSelecao(), contexto.getPerimetroSelecao()));
        } else {
            lblMedidas.setText("Área: —   Perímetro: —");
        }
        lblMensagem.setText(contexto.getMensagem());
    }
}
