package org.example.Controller;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

/** Estado "desenhar polígono": cada clique adiciona um vértice. */
public class FerramentaDesenhar implements Ferramenta {

    private static final double RAIO_FECHAMENTO = 10.0;

    @Override
    public void aoPressionarMouse(MouseEvent e, EditorContext ctx) {
    }

    @Override
    public void aoArrastarMouse(MouseEvent e, EditorContext ctx) {
    }

    @Override
    public void aoSoltarMouse(MouseEvent e, EditorContext ctx) {
    }

    @Override
    public void aoClicar(MouseEvent e, EditorContext ctx) {
        if (e.getButton() == MouseButton.SECONDARY) {
            ctx.finalizarFormaEmAndamento();
            return;
        }
        if (e.getButton() != MouseButton.PRIMARY) return;
        if (e.getClickCount() >= 2) {
            ctx.finalizarFormaEmAndamento();
            return;
        }
        double x = ctx.aplicarSnap(e.getX());
        double y = ctx.aplicarSnap(e.getY());
        FormaGeometrica f = ctx.getFormaEmAndamento();
        if (f == null) {
            ctx.iniciarPoligono();
            f = ctx.getFormaEmAndamento();
        } else if (f.getQuantidadePontos() >= 3
                && f.getPontos().get(0).distancia(new Ponto(x, y)) <= RAIO_FECHAMENTO) {
            ctx.finalizarFormaEmAndamento();
            return;
        }
        f.adicionarPonto(new Ponto(x, y));
        ctx.requestRedraw();
    }

    @Override
    public void aoMoverMouse(MouseEvent e, EditorContext ctx) {
        if (ctx.getFormaEmAndamento() != null) ctx.requestRedraw();
    }
}
