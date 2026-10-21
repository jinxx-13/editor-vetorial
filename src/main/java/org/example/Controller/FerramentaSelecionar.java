package org.example.Controller;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;

/** Estado "selecionar": seleção por clique/caixa e movimentação das formas. */
public class FerramentaSelecionar implements Ferramenta {

    private boolean arrastandoCaixa;
    private double selecaoStartX;
    private double selecaoStartY;
    private double ultimoX;
    private double ultimoY;
    private boolean moveu;

    @Override
    public void aoPressionarMouse(MouseEvent e, EditorContext ctx) {
        if (e.getButton() != MouseButton.PRIMARY) return;
        double x = e.getX(), y = e.getY();
        FormaGeometrica alvo = ctx.formaNoPonto(x, y);
        ultimoX = ctx.aplicarSnap(x);
        ultimoY = ctx.aplicarSnap(y);
        moveu = false;
        if (alvo != null) {
            arrastandoCaixa = false;
            if (e.isShiftDown()) ctx.alternarSelecao(alvo);
            else if (!ctx.estaSelecionada(alvo)) ctx.selecionarApenas(alvo);
        } else {
            arrastandoCaixa = true;
            selecaoStartX = x;
            selecaoStartY = y;
            if (!e.isShiftDown()) ctx.limparSelecao();
            ctx.setCaixaSelecao(x, y, x, y);
        }
    }

    @Override
    public void aoArrastarMouse(MouseEvent e, EditorContext ctx) {
        if (!e.isPrimaryButtonDown()) return;
        if (arrastandoCaixa) {
            ctx.setCaixaSelecao(selecaoStartX, selecaoStartY, e.getX(), e.getY());
        } else if (ctx.temSelecao()) {
            double nx = ctx.aplicarSnap(e.getX());
            double ny = ctx.aplicarSnap(e.getY());
            double dx = nx - ultimoX, dy = ny - ultimoY;
            if (dx != 0 || dy != 0) {
                if (!moveu) {
                    ctx.salvarEstado();
                    moveu = true;
                }
                ctx.transladarSelecao(dx, dy);
                ultimoX = nx;
                ultimoY = ny;
            }
        }
    }

    @Override
    public void aoSoltarMouse(MouseEvent e, EditorContext ctx) {
        if (arrastandoCaixa) {
            ctx.selecionarCaixa(selecaoStartX, selecaoStartY, e.getX(), e.getY(), e.isShiftDown());
            ctx.limparCaixaSelecao();
            arrastandoCaixa = false;
        }
    }

    @Override
    public void aoClicar(MouseEvent e, EditorContext ctx) {
    }
}
