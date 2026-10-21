package org.example.Models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/** Implementação base: forma definida por uma lista de vértices. */
public abstract class FormaBase implements FormaGeometrica {

    private static final Pattern HEX = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    protected List<Ponto> pontos = new ArrayList<>();
    protected String corPreenchimentoHex = "#D3D3D3";
    protected String corBordaHex = "#000000";
    protected double espessuraBorda = 1.0;
    protected String groupId;

    // ---------- pontos ----------
    @Override
    public void adicionarPonto(Ponto p) {
        pontos.add(Objects.requireNonNull(p, "O ponto não pode ser nulo."));
    }

    @Override
    public List<Ponto> getPontos() {
        return Collections.unmodifiableList(pontos);
    }

    /** Substitui todos os vértices (usado na persistência). */
    public void setPontos(List<Ponto> novos) {
        Objects.requireNonNull(novos, "Lista de pontos nula.");
        this.pontos = new ArrayList<>(novos);
    }

    @Override
    public int getQuantidadePontos() {
        return pontos.size();
    }

    /** Regra de negócio de validade da forma (sobrescrita nas subclasses). */
    public boolean isValida() {
        return pontos.size() >= 3;
    }

    // ---------- estilo ----------
    private static String validarHex(String hex) {
        if (hex == null || !HEX.matcher(hex).matches()) {
            throw new IllegalArgumentException("Cor hexadecimal inválida: " + hex);
        }
        return hex.toUpperCase();
    }

    @Override public String getCorPreenchimentoHex() { return corPreenchimentoHex; }
    @Override public void setCorPreenchimentoHex(String hex) { this.corPreenchimentoHex = validarHex(hex); }
    @Override public String getCorBordaHex() { return corBordaHex; }
    @Override public void setCorBordaHex(String hex) { this.corBordaHex = validarHex(hex); }
    @Override public double getEspessuraBorda() { return espessuraBorda; }

    @Override
    public void setEspessuraBorda(double espessura) {
        if (!(espessura > 0)) {
            throw new IllegalArgumentException("A espessura da borda deve ser positiva.");
        }
        this.espessuraBorda = espessura;
    }

    @Override public String getGroupId() { return groupId; }
    @Override public void setGroupId(String groupId) { this.groupId = groupId; }

    // ---------- geometria ----------
    /** Área pela fórmula de Gauss (shoelace). */
    @Override
    public double calcularArea() {
        int n = pontos.size();
        if (n < 3) return 0;
        double soma = 0;
        for (int i = 0; i < n; i++) {
            Ponto a = pontos.get(i);
            Ponto b = pontos.get((i + 1) % n);
            soma += a.getX() * b.getY() - b.getX() * a.getY();
        }
        return Math.abs(soma) / 2.0;
    }

    /** Perímetro: soma das distâncias euclidianas (polígono fechado). */
    @Override
    public double calcularPerimetro() {
        int n = pontos.size();
        if (n < 2) return 0;
        if (n == 2) return pontos.get(0).distancia(pontos.get(1));
        double soma = 0;
        for (int i = 0; i < n; i++) {
            soma += pontos.get(i).distancia(pontos.get((i + 1) % n));
        }
        return soma;
    }

    @Override
    public Ponto getCentroide() {
        if (pontos.isEmpty()) return new Ponto(0, 0);
        double sx = 0, sy = 0;
        for (Ponto p : pontos) {
            sx += p.getX();
            sy += p.getY();
        }
        return new Ponto(sx / pontos.size(), sy / pontos.size());
    }

    /** Teste ponto-em-polígono (ray casting). */
    @Override
    public boolean contemPonto(double px, double py) {
        int n = pontos.size();
        if (n < 3) return false;
        boolean dentro = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            Ponto a = pontos.get(i);
            Ponto b = pontos.get(j);
            if ((a.getY() > py) != (b.getY() > py)
                    && px < (b.getX() - a.getX()) * (py - a.getY()) / (b.getY() - a.getY()) + a.getX()) {
                dentro = !dentro;
            }
        }
        return dentro;
    }

    // ---------- transformações ----------
    @Override
    public void transladar(double dx, double dy) {
        for (Ponto p : pontos) p.mover(dx, dy);
    }

    /** Multiplica a distância de cada ponto ao centroide pelo fator. */
    @Override
    public void escalar(double fator) {
        if (!(fator > 0)) throw new IllegalArgumentException("O fator de escala deve ser positivo.");
        Ponto c = getCentroide();
        for (Ponto p : pontos) {
            p.setX(c.getX() + (p.getX() - c.getX()) * fator);
            p.setY(c.getY() + (p.getY() - c.getY()) * fator);
        }
    }

    /** Rotaciona em torno do centroide (radianos). */
    @Override
    public void rotacionar(double anguloRadianos) {
        Ponto c = getCentroide();
        double cos = Math.cos(anguloRadianos);
        double sin = Math.sin(anguloRadianos);
        for (Ponto p : pontos) {
            double dx = p.getX() - c.getX();
            double dy = p.getY() - c.getY();
            p.setX(c.getX() + dx * cos - dy * sin);
            p.setY(c.getY() + dx * sin + dy * cos);
        }
    }

    /** Cisalhamento em torno do centroide. */
    @Override
    public void cisalhar(double shx, double shy) {
        Ponto c = getCentroide();
        for (Ponto p : pontos) {
            double dx = p.getX() - c.getX();
            double dy = p.getY() - c.getY();
            p.setX(p.getX() + shx * dy);
            p.setY(p.getY() + shy * dx);
        }
    }

    /** Auxiliar do Prototype: copia pontos (profundo) e atributos para o destino. */
    protected void copiarPara(FormaBase destino) {
        List<Ponto> copia = new ArrayList<>();
        for (Ponto p : pontos) copia.add(p.clonar());
        destino.pontos = copia;
        destino.corPreenchimentoHex = corPreenchimentoHex;
        destino.corBordaHex = corBordaHex;
        destino.espessuraBorda = espessuraBorda;
        destino.groupId = groupId;
    }
}
