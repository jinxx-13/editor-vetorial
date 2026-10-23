package org.example.Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Hexagono;
import org.example.Models.Poligono;
import org.example.Models.Ponto;
import org.example.Models.Quadrado;

/**
 * State Pattern: contexto do editor. Mantém o modelo (formas, seleção) e delega
 * os eventos do mouse à {@link Ferramenta} atual.
 * Composite: formas com o mesmo groupId são tratadas como uma única forma lógica.
 */
public class EditorContext {

    public static final double TAMANHO_GRADE = 20.0;
    private static final double DESLOCAMENTO_COPIA = 20.0;

    public enum TipoForma { QUADRADO, CIRCULO, HEXAGONO }

    private final List<FormaGeometrica> formas = new ArrayList<>();
    private final List<FormaGeometrica> selecionados = new ArrayList<>();
    private FormaGeometrica formaEmAndamento;
    private Ferramenta ferramentaAtual;
    private boolean gridSnappingHabilitado;

    private final HistoricoController historico = new HistoricoController();
    private boolean alterado;

    private double cursorX, cursorY;
    private double[] caixaSelecao;
    private String mensagem = "";

    private String corPreenchimentoAtual = "#D3D3D3";
    private String corBordaAtual = "#000000";
    private double espessuraAtual = 1.0;
    private TipoForma tipoInsercao = TipoForma.QUADRADO;
    private final List<FormaGeometrica> areaTransferencia = new ArrayList<>();
    private int contadorColagem = 0;

    private Runnable redrawListener;
    private Runnable statsListener;

    // ------------------------------------------------------------ notificações
    public void setRedrawListener(Runnable r) { this.redrawListener = r; }
    public void setStatsListener(Runnable r) { this.statsListener = r; }

    public void requestRedraw() {
        if (redrawListener != null) redrawListener.run();
    }

    public void requestStatsUpdate() {
        if (statsListener != null) statsListener.run();
    }

    private void notificarTudo() {
        requestRedraw();
        requestStatsUpdate();
    }

    // ------------------------------------------------------------ acesso
    public List<FormaGeometrica> getFormas() { return Collections.unmodifiableList(formas); }
    public List<FormaGeometrica> getSelecionados() { return Collections.unmodifiableList(selecionados); }
    public FormaGeometrica getFormaEmAndamento() { return formaEmAndamento; }
    public void setFormaEmAndamento(FormaGeometrica f) { this.formaEmAndamento = f; }
    public Ferramenta getFerramentaAtual() { return ferramentaAtual; }

    /** Troca o estado (ferramenta). Finaliza o que estiver em andamento. */
    public void setFerramentaAtual(Ferramenta f) {
        finalizarFormaEmAndamento();
        this.ferramentaAtual = f;
        notificarTudo();
    }

    public boolean isGridSnappingHabilitado() { return gridSnappingHabilitado; }
    public void setGridSnappingHabilitado(boolean b) {
        this.gridSnappingHabilitado = b;
        notificarTudo();
    }

    public boolean isAlterado() { return alterado; }
    public void setAlterado(boolean b) { this.alterado = b; requestStatsUpdate(); }

    public double getCursorX() { return cursorX; }
    public double getCursorY() { return cursorY; }
    public void setCursor(double x, double y) { this.cursorX = x; this.cursorY = y; }

    public double[] getCaixaSelecao() { return caixaSelecao; }
    public void setCaixaSelecao(double x1, double y1, double x2, double y2) {
        this.caixaSelecao = new double[]{x1, y1, x2, y2};
        requestRedraw();
    }
    public void limparCaixaSelecao() { this.caixaSelecao = null; requestRedraw(); }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String m) { this.mensagem = m == null ? "" : m; requestStatsUpdate(); }

    public String getCorPreenchimentoAtual() { return corPreenchimentoAtual; }
    public void setCorPreenchimentoAtual(String hex) { this.corPreenchimentoAtual = hex; }
    public String getCorBordaAtual() { return corBordaAtual; }
    public void setCorBordaAtual(String hex) { this.corBordaAtual = hex; }
    public double getEspessuraAtual() { return espessuraAtual; }
    public void setEspessuraAtual(double e) { this.espessuraAtual = e; }
    public TipoForma getTipoInsercao() { return tipoInsercao; }
    public void setTipoInsercao(TipoForma t) { this.tipoInsercao = t; }

    // ------------------------------------------------------------ snap
    public double aplicarSnap(double valor) {
        if (!gridSnappingHabilitado) return valor;
        return Math.round(valor / TAMANHO_GRADE) * TAMANHO_GRADE;
    }

    // ------------------------------------------------------------ histórico
    /** Registra o estado ANTES de uma alteração (Memento). */
    public void salvarEstado() {
        historico.registrarEstado(formas);
        alterado = true;
    }

    public void desfazer() {
        if (!historico.podeDesfazer()) return;
        substituirFormas(historico.desfazer(formas));
    }

    public void refazer() {
        if (!historico.podeRefazer()) return;
        substituirFormas(historico.refazer(formas));
    }

    private void substituirFormas(List<FormaGeometrica> novas) {
        List<FormaGeometrica> copia = new ArrayList<>(novas);
        formas.clear();
        formas.addAll(copia);
        selecionados.clear();
        formaEmAndamento = null;
        alterado = true;
        notificarTudo();
    }

    /** Carrega um documento novo (limpa histórico e marca como salvo). */
    public void carregarDocumento(List<FormaGeometrica> novas, boolean grid) {
        formas.clear();
        formas.addAll(novas);
        selecionados.clear();
        formaEmAndamento = null;
        historico.limpar();
        gridSnappingHabilitado = grid;
        alterado = false;
        notificarTudo();
    }

    public void novoDocumento() {
        carregarDocumento(new ArrayList<>(), gridSnappingHabilitado);
    }

    // ------------------------------------------------------------ formas
    public void adicionarForma(FormaGeometrica f) {
        formas.add(f);
        notificarTudo();
    }

    private void aplicarEstiloAtual(FormaGeometrica f) {
        f.setCorPreenchimentoHex(corPreenchimentoAtual);
        f.setCorBordaHex(corBordaAtual);
        f.setEspessuraBorda(espessuraAtual);
    }

    public void iniciarPoligono() {
        Poligono p = new Poligono();
        aplicarEstiloAtual(p);
        this.formaEmAndamento = p;
    }

    public FormaGeometrica criarForma(TipoForma tipo, Ponto centro, double raio) {
        double r = Math.max(raio, 1.0);
        FormaGeometrica f;
        switch (tipo) {
            case CIRCULO: f = new Circulo(centro, r); break;
            case HEXAGONO: f = new Hexagono(centro, r); break;
            default: f = new Quadrado(centro, r); break;
        }
        aplicarEstiloAtual(f);
        return f;
    }

    /** Fecha o polígono em andamento; descarta se tiver menos de 3 pontos. */
    public void finalizarFormaEmAndamento() {
        FormaGeometrica f = formaEmAndamento;
        if (f == null) return;
        formaEmAndamento = null;
        if (f.getQuantidadePontos() >= 3) {
            salvarEstado();
            formas.add(f);
            mensagem = "";
        } else {
            mensagem = "Polígono descartado: são necessários ao menos 3 pontos.";
        }
        notificarTudo();
    }

    public void cancelarFormaEmAndamento() {
        formaEmAndamento = null;
        notificarTudo();
    }

    public FormaGeometrica formaNoPonto(double x, double y) {
        for (int i = formas.size() - 1; i >= 0; i--) {
            if (formas.get(i).contemPonto(x, y)) return formas.get(i);
        }
        return null;
    }

    // ------------------------------------------------------------ Composite (grupos)
    public List<FormaGeometrica> expandirGrupo(FormaGeometrica f) {
        List<FormaGeometrica> r = new ArrayList<>();
        String gid = f.getGroupId();
        if (gid == null) {
            r.add(f);
        } else {
            for (FormaGeometrica o : formas) if (gid.equals(o.getGroupId())) r.add(o);
            if (!r.contains(f)) r.add(f);
        }
        return r;
    }

    public void agruparSelecionados() {
        if (selecionados.size() < 2) {
            setMensagem("Selecione ao menos 2 formas para agrupar.");
            return;
        }
        salvarEstado();
        String id = UUID.randomUUID().toString();
        for (FormaGeometrica f : selecionados) f.setGroupId(id);
        notificarTudo();
    }

    public void desagruparSelecionados() {
        boolean algum = false;
        for (FormaGeometrica f : selecionados) if (f.getGroupId() != null) algum = true;
        if (!algum) return;
        salvarEstado();
        for (FormaGeometrica f : selecionados) f.setGroupId(null);
        notificarTudo();
    }

    // ------------------------------------------------------------ seleção
    public boolean estaSelecionada(FormaGeometrica f) { return selecionados.contains(f); }
    public boolean temSelecao() { return !selecionados.isEmpty(); }
    public FormaGeometrica getPrimeiraSelecionada() { return selecionados.isEmpty() ? null : selecionados.get(0); }

    public void limparSelecao() {
        selecionados.clear();
        notificarTudo();
    }

    public void selecionarApenas(FormaGeometrica f) {
        selecionados.clear();
        selecionados.addAll(expandirGrupo(f));
        notificarTudo();
    }

    public void alternarSelecao(FormaGeometrica f) {
        List<FormaGeometrica> grupo = expandirGrupo(f);
        if (estaSelecionada(f)) {
            selecionados.removeAll(grupo);
        } else {
            for (FormaGeometrica g : grupo) if (!selecionados.contains(g)) selecionados.add(g);
        }
        notificarTudo();
    }

    public void selecionarCaixa(double x1, double y1, double x2, double y2, boolean adicionar) {
        double minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        double minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        Set<FormaGeometrica> conjunto = new LinkedHashSet<>();
        if (adicionar) conjunto.addAll(selecionados);
        for (FormaGeometrica f : formas) {
            boolean dentro = false;
            Ponto c = f.getCentroide();
            if (c.getX() >= minX && c.getX() <= maxX && c.getY() >= minY && c.getY() <= maxY) dentro = true;
            if (!dentro) {
                for (Ponto p : f.getPontos()) {
                    if (p.getX() >= minX && p.getX() <= maxX && p.getY() >= minY && p.getY() <= maxY) {
                        dentro = true;
                        break;
                    }
                }
            }
            if (dentro) conjunto.addAll(expandirGrupo(f));
        }
        selecionados.clear();
        selecionados.addAll(conjunto);
        notificarTudo();
    }

    // ------------------------------------------------------------ camadas / exclusão
    private List<FormaGeometrica> selecionadasEmOrdem() {
        List<FormaGeometrica> r = new ArrayList<>();
        for (FormaGeometrica f : formas) if (selecionados.contains(f)) r.add(f);
        return r;
    }

    public void trazerParaFrente() {
        if (selecionados.isEmpty()) return;
        salvarEstado();
        List<FormaGeometrica> sel = selecionadasEmOrdem();
        formas.removeAll(sel);
        formas.addAll(sel);
        notificarTudo();
    }

    public void enviarParaTras() {
        if (selecionados.isEmpty()) return;
        salvarEstado();
        List<FormaGeometrica> sel = selecionadasEmOrdem();
        formas.removeAll(sel);
        formas.addAll(0, sel);
        notificarTudo();
    }

    public void excluirSelecionados() {
        if (selecionados.isEmpty()) return;
        salvarEstado();
        formas.removeAll(selecionados);
        selecionados.clear();
        notificarTudo();
    }

    public void excluirForma(FormaGeometrica f) {
        salvarEstado();
        List<FormaGeometrica> grupo = expandirGrupo(f);
        formas.removeAll(grupo);
        selecionados.removeAll(grupo);
        notificarTudo();
    }

    // ------------------------------------------------------------ duplicar / copiar / colar (Prototype)
    /** Clona as formas; cada grupo original vira um grupo novo (um UUID por grupo). */
    private List<FormaGeometrica> clonarComGrupos(List<FormaGeometrica> origem) {
        Map<String, String> novosGrupos = new HashMap<>();
        List<FormaGeometrica> copias = new ArrayList<>();
        for (FormaGeometrica f : origem) {
            FormaGeometrica c = f.clonar();
            String gid = f.getGroupId();
            if (gid != null) {
                c.setGroupId(novosGrupos.computeIfAbsent(gid, k -> UUID.randomUUID().toString()));
            }
            copias.add(c);
        }
        return copias;
    }

    /** Ctrl+D: duplica a seleção com um pequeno deslocamento. */
    public void duplicarSelecionados() {
        if (selecionados.isEmpty()) return;
        salvarEstado();
        List<FormaGeometrica> copias = clonarComGrupos(selecionadasEmOrdem());
        for (FormaGeometrica c : copias) c.transladar(DESLOCAMENTO_COPIA, DESLOCAMENTO_COPIA);
        formas.addAll(copias);
        selecionados.clear();
        selecionados.addAll(copias);
        notificarTudo();
    }

    /** Ctrl+C: guarda uma cópia da seleção na área de transferência interna. */
    public void copiarSelecionados() {
        if (selecionados.isEmpty()) return;
        areaTransferencia.clear();
        areaTransferencia.addAll(clonarComGrupos(selecionadasEmOrdem()));
        contadorColagem = 0;
        setMensagem(areaTransferencia.size() + " forma(s) copiada(s).");
    }

    /** Ctrl+V: cola as formas copiadas, deslocando um pouco mais a cada colagem. */
    public void colarFormas() {
        if (areaTransferencia.isEmpty()) return;
        salvarEstado();
        contadorColagem++;
        double d = DESLOCAMENTO_COPIA * contadorColagem;
        List<FormaGeometrica> copias = clonarComGrupos(areaTransferencia);
        for (FormaGeometrica c : copias) c.transladar(d, d);
        formas.addAll(copias);
        selecionados.clear();
        selecionados.addAll(copias);
        notificarTudo();
    }

    // ------------------------------------------------------------ transformações da seleção
    public Ponto getCentroideSelecao() {
        if (selecionados.isEmpty()) return new Ponto(0, 0);
        double sx = 0, sy = 0;
        for (FormaGeometrica f : selecionados) {
            Ponto c = f.getCentroide();
            sx += c.getX();
            sy += c.getY();
        }
        return new Ponto(sx / selecionados.size(), sy / selecionados.size());
    }

    public void transladarSelecao(double dx, double dy) {
        for (FormaGeometrica f : selecionados) f.transladar(dx, dy);
        notificarTudo();
    }

    /** Escala o conjunto (grupo) em torno do centroide comum. */
    public void escalarSelecao(double fator) {
        if (selecionados.isEmpty() || !(fator > 0)) return;
        Ponto pivo = getCentroideSelecao();
        for (FormaGeometrica f : selecionados) {
            Ponto c = f.getCentroide();
            f.escalar(fator);
            Ponto c2 = f.getCentroide();
            double alvoX = pivo.getX() + (c.getX() - pivo.getX()) * fator;
            double alvoY = pivo.getY() + (c.getY() - pivo.getY()) * fator;
            f.transladar(alvoX - c2.getX(), alvoY - c2.getY());
        }
        notificarTudo();
    }

    /** Rotaciona o conjunto (grupo) em torno do centroide comum. */
    public void rotacionarSelecao(double ang) {
        if (selecionados.isEmpty()) return;
        Ponto pivo = getCentroideSelecao();
        double cos = Math.cos(ang), sin = Math.sin(ang);
        for (FormaGeometrica f : selecionados) {
            Ponto c = f.getCentroide();
            f.rotacionar(ang);
            Ponto c2 = f.getCentroide();
            double dx = c.getX() - pivo.getX();
            double dy = c.getY() - pivo.getY();
            double alvoX = pivo.getX() + dx * cos - dy * sin;
            double alvoY = pivo.getY() + dx * sin + dy * cos;
            f.transladar(alvoX - c2.getX(), alvoY - c2.getY());
        }
        notificarTudo();
    }

    /** Cisalha o conjunto (grupo) em torno do centroide comum. */
    public void cisalharSelecao(double shx, double shy) {
        if (selecionados.isEmpty()) return;
        Ponto pivo = getCentroideSelecao();
        for (FormaGeometrica f : selecionados) {
            Ponto c = f.getCentroide();
            f.cisalhar(shx, shy);
            Ponto c2 = f.getCentroide();
            double alvoX = c.getX() + shx * (c.getY() - pivo.getY());
            double alvoY = c.getY() + shy * (c.getX() - pivo.getX());
            f.transladar(alvoX - c2.getX(), alvoY - c2.getY());
        }
        notificarTudo();
    }

    // ------------------------------------------------------------ estilo na seleção
    public void aplicarCorPreenchimentoSelecao(String hex) {
        if (selecionados.isEmpty()) return;
        salvarEstado();
        for (FormaGeometrica f : selecionados) f.setCorPreenchimentoHex(hex);
        notificarTudo();
    }

    public void aplicarCorBordaSelecao(String hex) {
        if (selecionados.isEmpty()) return;
        salvarEstado();
        for (FormaGeometrica f : selecionados) f.setCorBordaHex(hex);
        notificarTudo();
    }

    public void aplicarEspessuraSelecao(double valor, boolean registrar) {
        if (selecionados.isEmpty()) return;
        if (registrar) salvarEstado();
        for (FormaGeometrica f : selecionados) f.setEspessuraBorda(valor);
        notificarTudo();
    }

    // ------------------------------------------------------------ estatísticas
    public double getAreaSelecao() {
        double s = 0;
        for (FormaGeometrica f : selecionados) s += f.calcularArea();
        return s;
    }

    public double getPerimetroSelecao() {
        double s = 0;
        for (FormaGeometrica f : selecionados) s += f.calcularPerimetro();
        return s;
    }
}
