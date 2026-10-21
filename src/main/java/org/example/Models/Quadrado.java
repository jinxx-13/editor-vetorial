package org.example.Models;

/** Quadrado regular. O "raio" é a metade do lado; os 4 vértices são calculados automaticamente. */
public class Quadrado extends FormaBase {

    public Quadrado(Ponto centro, double raio) {
        if (centro == null) throw new IllegalArgumentException("O centro não pode ser nulo.");
        if (!(raio > 0)) throw new IllegalArgumentException("O raio deve ser estritamente positivo (r > 0).");
        double cx = centro.getX(), cy = centro.getY();
        pontos.add(new Ponto(cx - raio, cy - raio));
        pontos.add(new Ponto(cx + raio, cy - raio));
        pontos.add(new Ponto(cx + raio, cy + raio));
        pontos.add(new Ponto(cx - raio, cy + raio));
    }

    @Override
    public FormaGeometrica clonar() {
        Quadrado c = new Quadrado(new Ponto(0, 0), 1);
        copiarPara(c);
        return c;
    }
}
