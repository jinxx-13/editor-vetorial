package org.example.Models;

/** Círculo: um ponto (centro) e raio estritamente positivo. */
public class Circulo extends FormaBase {

    private double raio;

    public Circulo(Ponto centro, double raio) {
        if (centro == null) throw new IllegalArgumentException("O centro não pode ser nulo.");
        validarRaio(raio);
        this.pontos.add(centro);
        this.raio = raio;
    }

    private static void validarRaio(double r) {
        if (!(r > 0)) throw new IllegalArgumentException("O raio deve ser estritamente positivo (r > 0).");
    }

    public double getRaio() { return raio; }

    public void setRaio(double raio) {
        validarRaio(raio);
        this.raio = raio;
    }

    @Override
    public void adicionarPonto(Ponto p) {
        throw new UnsupportedOperationException("Um círculo possui apenas o ponto central.");
    }

    @Override
    public boolean isValida() {
        return pontos.size() == 1 && raio > 0;
    }

    @Override
    public double calcularArea() {
        return Math.PI * raio * raio;
    }

    @Override
    public double calcularPerimetro() {
        return 2 * Math.PI * raio;
    }

    @Override
    public Ponto getCentroide() {
        return pontos.get(0).clonar();
    }

    @Override
    public boolean contemPonto(double px, double py) {
        Ponto c = pontos.get(0);
        return Math.hypot(px - c.getX(), py - c.getY()) <= raio;
    }

    @Override
    public void escalar(double fator) {
        if (!(fator > 0)) throw new IllegalArgumentException("O fator de escala deve ser positivo.");
        this.raio *= fator;
    }

    /** Rotação em torno do próprio centro não altera um círculo. */
    @Override
    public void rotacionar(double anguloRadianos) {
        // sem efeito
    }

    /** Cisalhar em torno do próprio centro mantém o círculo. */
    @Override
    public void cisalhar(double shx, double shy) {
        // sem efeito
    }

    @Override
    public FormaGeometrica clonar() {
        Circulo c = new Circulo(pontos.get(0).clonar(), raio);
        c.corPreenchimentoHex = corPreenchimentoHex;
        c.corBordaHex = corBordaHex;
        c.espessuraBorda = espessuraBorda;
        c.groupId = groupId;
        return c;
    }
}
