package org.example.Controller;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

import java.util.ArrayList;
import java.util.List;

/** Estado "editar vértice": arrasta um vértice de uma forma poligonal. */
public class FerramentaEditarVertice implements Ferramenta {

    private static final double RAIO_CAPTURA = 10.0;

    private Ponto verticeSelecionado;
    private boolean salvou;

    @Override
    public void aoPressionarMouse(MouseEvent e, EditorContext ctx) {
        if (e.getButton() != MouseButton.PRIMARY) return;
        verticeSelecionado = null;
        salvou = false;
        double x = e.getX(), y = e.getY();
        List<FormaGeometrica> candidatas = new ArrayList<>(
                ctx.getSelecionados().isEmpty() ? ctx.getFormas() : ctx.getSelecionados());
        for (int i = candidatas.size() - 1; i >= 0; i--) {
            FormaGeometrica f = candidatas.get(i);
            if (f instanceof Circulo) continue;
            for (Ponto p : f.getPontos()) {
                if (Math.hypot(p.getX() - x, p.getY() - y) <= RAIO_CAPTURA) {
                    verticeSelecionado = p;
                    if (!ctx.estaSelecionada(f)) ctx.selecionarApenas(f);
                    ctx.requestRedraw();
                    return;
                }
            }
        }
        FormaGeometrica alvo = ctx.formaNoPonto(x, y);
        if (alvo != null) ctx.selecionarApenas(alvo);
        else ctx.limparSelecao();
    }

    @Override
    public void aoArrastarMouse(MouseEvent e, EditorContext ctx) {
        if (verticeSelecionado == null) return;
        double nx = ctx.aplicarSnap(e.getX());
        double ny = ctx.aplicarSnap(e.getY());
        if (nx == verticeSelecionado.getX() && ny == verticeSelecionado.getY()) return;
        if (!salvou) {
            ctx.salvarEstado();
            salvou = true;
        }
        verticeSelecionado.setX(nx);
        verticeSelecionado.setY(ny);
        ctx.requestRedraw();
        ctx.requestStatsUpdate();
    }

    @Override
    public void aoSoltarMouse(MouseEvent e, EditorContext ctx) {
        verticeSelecionado = null;
    }

    @Override
    public void aoClicar(MouseEvent e, EditorContext ctx) {
    }
}
