package org.example.Models;

/** Ponto 2D mutável. */
public class Ponto {
    private double x;
    private double y;

    public Ponto(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public void mover(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }

    public double distancia(Ponto outro) {
        return Math.hypot(x - outro.x, y - outro.y);
    }

    /** Prototype: cópia independente do ponto. */
    public Ponto clonar() {
        return new Ponto(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
