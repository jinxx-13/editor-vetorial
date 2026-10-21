package org.example.Controller;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;

/** Estado "excluir": clique sobre a forma (ou grupo) para removê-la. */
public class FerramentaExcluir implements Ferramenta {

    @Override
    public void aoPressionarMouse(MouseEvent e, EditorContext ctx) {
        if (e.getButton() != MouseButton.PRIMARY) return;
        FormaGeometrica alvo = ctx.formaNoPonto(e.getX(), e.getY());
        if (alvo != null) ctx.excluirForma(alvo);
    }

    @Override
    public void aoArrastarMouse(MouseEvent e, EditorContext ctx) {
    }

    @Override
    public void aoSoltarMouse(MouseEvent e, EditorContext ctx) {
    }

    @Override
    public void aoClicar(MouseEvent e, EditorContext ctx) {
    }
}
