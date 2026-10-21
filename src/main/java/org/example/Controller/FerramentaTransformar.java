package org.example.Controller;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

/** Estado "transformar": escala, rotação ou cisalhamento por arrasto do mouse. */
public class FerramentaTransformar implements Ferramenta {

    private final ModoFerramenta modo;
    private Ponto pivo;
    private double ultimoX;
    private double ultimoY;
    private boolean ativo;
    private boolean salvou;

    public FerramentaTransformar(ModoFerramenta modo) {
        this.modo = modo;
    }

    public ModoFerramenta getModo() {
        return modo;
    }

    @Override
    public void aoPressionarMouse(MouseEvent e, EditorContext ctx) {
        if (e.getButton() != MouseButton.PRIMARY) return;
        FormaGeometrica alvo = ctx.formaNoPonto(e.getX(), e.getY());
        if (alvo != null && !ctx.estaSelecionada(alvo)) ctx.selecionarApenas(alvo);
        if (!ctx.temSelecao()) {
            ativo = false;
            return;
        }
        pivo = ctx.getCentroideSelecao();
        ultimoX = e.getX();
        ultimoY = e.getY();
        salvou = false;
        ativo = true;
    }

    @Override
    public void aoArrastarMouse(MouseEvent e, EditorContext ctx) {
        if (!ativo || !ctx.temSelecao()) return;
        double x = e.getX(), y = e.getY();
        boolean mudou = false;
        switch (modo) {
            case ESCALAR: {
                double d0 = Math.hypot(ultimoX - pivo.getX(), ultimoY - pivo.getY());
                double d1 = Math.hypot(x - pivo.getX(), y - pivo.getY());
                if (d0 > 1e-6) {
                    double f = d1 / d0;
                    if (f > 0.05 && f < 20) {
                        registrar(ctx);
                        ctx.escalarSelecao(f);
                        mudou = true;
                    }
                }
                break;
            }
            case ROTACIONAR: {
                double a0 = Math.atan2(ultimoY - pivo.getY(), ultimoX - pivo.getX());
                double a1 = Math.atan2(y - pivo.getY(), x - pivo.getX());
                double delta = a1 - a0;
                if (delta != 0) {
                    registrar(ctx);
                    ctx.rotacionarSelecao(delta);
                    mudou = true;
                }
                break;
            }
            case CISALHAR: {
                double dx = x - ultimoX, dy = y - ultimoY;
                if (dx != 0 || dy != 0) {
                    registrar(ctx);
                    if (Math.abs(dx) >= Math.abs(dy)) ctx.cisalharSelecao(dx / 150.0, 0);
                    else ctx.cisalharSelecao(0, dy / 150.0);
                    mudou = true;
                }
                break;
            }
            default:
                break;
        }
        if (mudou) {
            ultimoX = x;
            ultimoY = y;
        }
    }

    private void registrar(EditorContext ctx) {
        if (!salvou) {
            ctx.salvarEstado();
            salvou = true;
        }
    }

    @Override
    public void aoSoltarMouse(MouseEvent e, EditorContext ctx) {
        ativo = false;
    }

    @Override
    public void aoClicar(MouseEvent e, EditorContext ctx) {
    }
}
