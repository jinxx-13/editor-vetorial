package org.example.Controller;

import javafx.scene.input.MouseEvent;

/** State Pattern: interface de estado. Cada ferramenta define como o mouse é tratado. */
public interface Ferramenta {
    void aoPressionarMouse(MouseEvent e, EditorContext ctx);
    void aoArrastarMouse(MouseEvent e, EditorContext ctx);
    void aoSoltarMouse(MouseEvent e, EditorContext ctx);
    void aoClicar(MouseEvent e, EditorContext ctx);

    /** Movimento sem botão pressionado (opcional). */
    default void aoMoverMouse(MouseEvent e, EditorContext ctx) {
    }
}
