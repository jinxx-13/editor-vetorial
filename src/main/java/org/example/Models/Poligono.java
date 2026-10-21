package org.example.Models;

/** Polígono livre: no mínimo 3 pontos. */
public class Poligono extends FormaBase {

    public Poligono() {
    }

    /** Lança exceção se o polígono tiver menos de 3 pontos. */
    public void validar() {
        if (pontos.size() < 3) {
            throw new IllegalStateException("Um polígono deve ter no mínimo 3 pontos.");
        }
    }

    @Override
    public FormaGeometrica clonar() {
        Poligono c = new Poligono();
        copiarPara(c);
        return c;
    }
}
