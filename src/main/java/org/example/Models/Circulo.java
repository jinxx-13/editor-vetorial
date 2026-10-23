package org.example.Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Círculo: um ponto (centro) e raio estritamente positivo.
 * Ao ser cisalhado vira uma elipse: os pontos do contorno são
 * centro + M * (raio*cos t, raio*sen t), onde M é a matriz 2x2 [a b; c d].
 */
public class Circulo extends FormaBase {

    private double raio;
    private double a = 1, b = 0, c = 0, d = 1; // matriz de deformação (identidade = círculo)

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

    // ---------- matriz de deformação ----------
    public double[] getMatriz() {
        return new double[]{a, b, c, d};
    }

    public void setMatriz(double a, double b, double c, double d) {
        double det = a * d - b * c;
        if (!Double.isFinite(det) || Math.abs(det) < 1e-9) {
            throw new IllegalArgumentException("Matriz de deformação inválida.");
        }
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    /** True se a forma deixou de ser um círculo (virou elipse). */
    public boolean isDeformado() {
        double p = a * a + c * c;
        double q = b * b + d * d;
        double s = a * b + c * d;
        return Math.abs(s) > 1e-9 || Math.abs(p - q) > 1e-9;
    }

    /** Contorno aproximado por n pontos (usado para desenhar a elipse). */
    public List<Ponto> getContorno(int n) {
        Ponto ct = pontos.get(0);
        List<Ponto> r = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            double t = 2 * Math.PI * i / n;
            double ux = raio * Math.cos(t);
            double uy = raio * Math.sin(t);
            r.add(new Ponto(ct.getX() + a * ux + b * uy, ct.getY() + c * ux + d * uy));
        }
        return r;
    }

    // ---------- regras da forma ----------
    @Override
    public void adicionarPonto(Ponto p) {
        throw new UnsupportedOperationException("Um círculo possui apenas o ponto central.");
    }

    @Override
    public boolean isValida() {
        return pontos.size() == 1 && raio > 0;
    }

    /** Área = pi * r^2 * |det(M)|. */
    @Override
    public double calcularArea() {
        return Math.PI * raio * raio * Math.abs(a * d - b * c);
    }

    /** Perímetro: círculo exato; elipse pela aproximação de Ramanujan. */
    @Override
    public double calcularPerimetro() {
        double p = a * a + c * c;
        double q = b * b + d * d;
        double s = a * b + c * d;
        double tr = p + q;
        double det2 = p * q - s * s;
        double disc = Math.sqrt(Math.max(0, tr * tr - 4 * det2));
        double l1 = (tr + disc) / 2;
        double l2 = Math.max(0, (tr - disc) / 2);
        double semiA = raio * Math.sqrt(l1);
        double semiB = raio * Math.sqrt(l2);
        return Math.PI * (3 * (semiA + semiB) - Math.sqrt((3 * semiA + semiB) * (semiA + 3 * semiB)));
    }

    @Override
    public Ponto getCentroide() {
        return pontos.get(0).clonar();
    }

    /** Leva o ponto para o espaço do círculo original (M^-1) e compara com o raio. */
    @Override
    public boolean contemPonto(double px, double py) {
        Ponto centro = pontos.get(0);
        double dx = px - centro.getX();
        double dy = py - centro.getY();
        double det = a * d - b * c;
        if (Math.abs(det) < 1e-9) return false;
        double lx = (d * dx - b * dy) / det;
        double ly = (-c * dx + a * dy) / det;
        return lx * lx + ly * ly <= raio * raio;
    }

    @Override
    public void escalar(double fator) {
        if (!(fator > 0)) throw new IllegalArgumentException("O fator de escala deve ser positivo.");
        this.raio *= fator;
    }

    /** Rotaciona a deformação em torno do centro: M = R * M. */
    @Override
    public void rotacionar(double anguloRadianos) {
        double cos = Math.cos(anguloRadianos);
        double sin = Math.sin(anguloRadianos);
        double na = cos * a - sin * c;
        double nb = cos * b - sin * d;
        double nc = sin * a + cos * c;
        double nd = sin * b + cos * d;
        a = na;
        b = nb;
        c = nc;
        d = nd;
    }

    /** Cisalha em torno do centro: M = S * M, com S = [1 shx; shy 1]. */
    @Override
    public void cisalhar(double shx, double shy) {
        double na = a + shx * c;
        double nb = b + shx * d;
        double nc = shy * a + c;
        double nd = shy * b + d;
        if (Math.abs(na * nd - nb * nc) < 1e-6) return; // evita achatar a forma até virar uma linha
        a = na;
        b = nb;
        c = nc;
        d = nd;
    }

    @Override
    public FormaGeometrica clonar() {
        Circulo copia = new Circulo(pontos.get(0).clonar(), raio);
        copia.a = a;
        copia.b = b;
        copia.c = c;
        copia.d = d;
        copia.corPreenchimentoHex = corPreenchimentoHex;
        copia.corBordaHex = corBordaHex;
        copia.espessuraBorda = espessuraBorda;
        copia.groupId = groupId;
        return copia;
    }
}