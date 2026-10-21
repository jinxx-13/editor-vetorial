package org.example.Models;

/** Hexágono regular: 6 vértices a distância "raio" do centro, calculados automaticamente. */
public class Hexagono extends FormaBase {

    public Hexagono(Ponto centro, double raio) {
        if (centro == null) throw new IllegalArgumentException("O centro não pode ser nulo.");
        if (!(raio > 0)) throw new IllegalArgumentException("O raio deve ser estritamente positivo (r > 0).");
        for (int i = 0; i < 6; i++) {
            double ang = Math.toRadians(60.0 * i);
            pontos.add(new Ponto(centro.getX() + raio * Math.cos(ang),
                                 centro.getY() + raio * Math.sin(ang)));
        }
    }

    @Override
    public FormaGeometrica clonar() {
        Hexagono c = new Hexagono(new Ponto(0, 0), 1);
        copiarPara(c);
        return c;
    }
}
