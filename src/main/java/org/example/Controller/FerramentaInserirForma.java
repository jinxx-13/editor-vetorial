package org.example.Controller;

import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/** Estado "inserir forma regular": clique (tamanho padrão) ou arraste (define o raio). */
public class FerramentaInserirForma implements Ferramenta {

    private static final double RAIO_PADRAO = 50.0;
    private static final double ARRASTO_MINIMO = 1.0;

    private Ponto centroInsercao;
    private boolean arrastou;

    private double raio(MouseEvent e, EditorContext ctx) {
        Ponto p = new Ponto(ctx.aplicarSnap(e.getX()), ctx.aplicarSnap(e.getY()));
        return p.distancia(centroInsercao);
    }

    @Override
    public void aoPressionarMouse(MouseEvent e, EditorContext ctx) {
        if (e.getButton() != MouseButton.PRIMARY) return;
        centroInsercao = new Ponto(ctx.aplicarSnap(e.getX()), ctx.aplicarSnap(e.getY()));
        arrastou = false;
    }

    @Override
    public void aoArrastarMouse(MouseEvent e, EditorContext ctx) {
        if (centroInsercao == null) return;
        double r = raio(e, ctx);
        if (r >= ARRASTO_MINIMO) {
            arrastou = true;
            ctx.setFormaEmAndamento(ctx.criarForma(ctx.getTipoInsercao(), centroInsercao, r));
            ctx.requestRedraw();
        }
    }

    @Override
    public void aoSoltarMouse(MouseEvent e, EditorContext ctx) {
        if (centroInsercao == null) return;
        double r = raio(e, ctx);
        if (!arrastou || r < ARRASTO_MINIMO) r = RAIO_PADRAO;
        ctx.setFormaEmAndamento(null);
        ctx.salvarEstado();
        FormaGeometrica nova = ctx.criarForma(ctx.getTipoInsercao(), centroInsercao, r);
        ctx.adicionarForma(nova);
        centroInsercao = null;
        arrastou = false;
    }

    @Override
    public void aoClicar(MouseEvent e, EditorContext ctx) {
    }
}
