package org.example.Controller;

import org.example.Models.FormaGeometrica;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/** Memento (desfazer/refazer): guarda cópias profundas da lista de formas. */
public class HistoricoController {

    private static final int LIMITE = 100;

    private final Stack<List<FormaGeometrica>> undoStack = new Stack<>();
    private final Stack<List<FormaGeometrica>> redoStack = new Stack<>();

    /** Salva o estado atual (antes de uma alteração). */
    public void registrarEstado(List<FormaGeometrica> formas) {
        undoStack.push(copiaProfunda(formas));
        if (undoStack.size() > LIMITE) undoStack.remove(0);
        redoStack.clear();
    }

    /** Retorna o estado anterior; o atual vai para a pilha de refazer. */
    public List<FormaGeometrica> desfazer(List<FormaGeometrica> atual) {
        if (undoStack.isEmpty()) return atual;
        redoStack.push(copiaProfunda(atual));
        return undoStack.pop();
    }

    /** Retorna o estado desfeito; o atual vai para a pilha de desfazer. */
    public List<FormaGeometrica> refazer(List<FormaGeometrica> atual) {
        if (redoStack.isEmpty()) return atual;
        undoStack.push(copiaProfunda(atual));
        return redoStack.pop();
    }

    public boolean podeDesfazer() { return !undoStack.isEmpty(); }
    public boolean podeRefazer() { return !redoStack.isEmpty(); }

    public void limpar() {
        undoStack.clear();
        redoStack.clear();
    }

    /** Prototype em ação: cada forma é duplicada com clonar(). */
    public static List<FormaGeometrica> copiaProfunda(List<FormaGeometrica> formas) {
        List<FormaGeometrica> copia = new ArrayList<>();
        for (FormaGeometrica f : formas) copia.add(f.clonar());
        return copia;
    }
}
