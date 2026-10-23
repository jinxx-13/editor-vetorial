package org.example.Views;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.example.Controller.ArquivoInvalidoException;
import org.example.Controller.EditorContext;
import org.example.Controller.Ferramenta;
import org.example.Controller.FerramentaEditarVertice;
import org.example.Controller.PersistenciaService;
import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Poligono;
import org.example.Models.Ponto;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/** Janela principal: monta a interface, o canvas e liga os eventos ao EditorContext. */
public class ContainerApp extends Application {

    private static final Color COR_SELECAO = Color.web("#00B050");

    private Stage stage;
    private Canvas areaDeDesenho;
    private EditorContext contexto;
    private BarraLateralView barraLateral;
    private PainelStatusView painelStatus;
    private File arquivoAtual;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        this.contexto = new EditorContext();
        this.areaDeDesenho = new Canvas(900, 650);
        this.painelStatus = new PainelStatusView(contexto);
        this.barraLateral = new BarraLateralView(contexto, this::salvarArquivo, this::carregarArquivo, this::novoDocumento);

        Pane centro = new Pane(areaDeDesenho);
        centro.setMinSize(0, 0);
        areaDeDesenho.widthProperty().bind(centro.widthProperty());
        areaDeDesenho.heightProperty().bind(centro.heightProperty());
        areaDeDesenho.widthProperty().addListener((o, a, n) -> redesenharCanvas());
        areaDeDesenho.heightProperty().addListener((o, a, n) -> redesenharCanvas());

        ScrollPane scrollLateral = new ScrollPane(barraLateral);
        scrollLateral.setFitToWidth(true);
        scrollLateral.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollLateral.setPrefWidth(205);

        BorderPane raiz = new BorderPane();
        raiz.setLeft(scrollLateral);
        raiz.setCenter(centro);
        raiz.setBottom(painelStatus);

        contexto.setRedrawListener(this::redesenharCanvas);
        contexto.setStatsListener(() -> {
            painelStatus.atualizar();
            barraLateral.atualizarCoresInterface();
            atualizarTitulo();
        });

        inicializarEventos();

        Scene cena = new Scene(raiz, 1150, 720);
        cena.setOnKeyPressed(this::aoTeclar);
        stage.setScene(cena);
        stage.setOnCloseRequest(e -> {
            if (!confirmarDescarte()) e.consume();
        });
        atualizarTitulo();
        stage.show();
        redesenharCanvas();
    }

    // ------------------------------------------------------------ eventos
    public void inicializarEventos() {
        areaDeDesenho.setOnMousePressed(e -> {
            areaDeDesenho.requestFocus();
            contexto.setMensagem("");
            atualizarCursor(e);
            Ferramenta f = contexto.getFerramentaAtual();
            if (f != null) f.aoPressionarMouse(e, contexto);
        });
        areaDeDesenho.setOnMouseDragged(e -> {
            atualizarCursor(e);
            Ferramenta f = contexto.getFerramentaAtual();
            if (f != null) f.aoArrastarMouse(e, contexto);
        });
        areaDeDesenho.setOnMouseReleased(e -> {
            atualizarCursor(e);
            Ferramenta f = contexto.getFerramentaAtual();
            if (f != null) f.aoSoltarMouse(e, contexto);
        });
        areaDeDesenho.setOnMouseClicked(e -> {
            Ferramenta f = contexto.getFerramentaAtual();
            if (f != null) f.aoClicar(e, contexto);
        });
        areaDeDesenho.setOnMouseMoved(e -> {
            atualizarCursor(e);
            Ferramenta f = contexto.getFerramentaAtual();
            if (f != null) f.aoMoverMouse(e, contexto);
        });
    }

    private void atualizarCursor(MouseEvent e) {
        contexto.setCursor(contexto.aplicarSnap(e.getX()), contexto.aplicarSnap(e.getY()));
        contexto.requestStatsUpdate();
    }

    private void aoTeclar(KeyEvent e) {
        if (e.isShortcutDown()) {
            if (e.getCode() == KeyCode.Z && !e.isShiftDown()) {
                contexto.desfazer();
                e.consume();
            } else if (e.getCode() == KeyCode.Y || (e.getCode() == KeyCode.Z && e.isShiftDown())) {
                contexto.refazer();
                e.consume();
            } else if (e.getCode() == KeyCode.S) {
                salvarArquivo();
                e.consume();
            } else if (e.getCode() == KeyCode.D) {
                contexto.duplicarSelecionados();
                e.consume();
            } else if (e.getCode() == KeyCode.C) {
                contexto.copiarSelecionados();
                e.consume();
            } else if (e.getCode() == KeyCode.V) {
                contexto.colarFormas();
                e.consume();
            }
            return;
        }
        switch (e.getCode()) {
            case DELETE:
            case BACK_SPACE:
                contexto.excluirSelecionados();
                break;
            case ESCAPE:
                contexto.cancelarFormaEmAndamento();
                break;
            case ENTER:
                contexto.finalizarFormaEmAndamento();
                break;
            default:
                break;
        }
    }

    // ------------------------------------------------------------ desenho
    private void redesenharCanvas() {
        if (areaDeDesenho == null || contexto == null) return;
        GraphicsContext g = areaDeDesenho.getGraphicsContext2D();
        double w = areaDeDesenho.getWidth(), h = areaDeDesenho.getHeight();
        g.setLineDashes((double[]) null);
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, w, h);

        if (contexto.isGridSnappingHabilitado()) {
            g.setStroke(Color.web("#E6E6E6"));
            g.setLineWidth(1);
            for (double x = 0; x <= w; x += EditorContext.TAMANHO_GRADE) g.strokeLine(x, 0, x, h);
            for (double y = 0; y <= h; y += EditorContext.TAMANHO_GRADE) g.strokeLine(0, y, w, y);
        }

        for (FormaGeometrica f : contexto.getFormas()) {
            desenharForma(f, contexto.estaSelecionada(f));
        }

        FormaGeometrica andamento = contexto.getFormaEmAndamento();
        if (andamento != null) {
            desenharForma(andamento, false);
            if (andamento instanceof Poligono && andamento.getQuantidadePontos() > 0) {
                List<Ponto> pts = andamento.getPontos();
                Ponto ultimo = pts.get(pts.size() - 1);
                g.setStroke(Color.GRAY);
                g.setLineWidth(1);
                g.setLineDashes(5, 4);
                g.strokeLine(ultimo.getX(), ultimo.getY(), contexto.getCursorX(), contexto.getCursorY());
                g.setLineDashes((double[]) null);
            }
        }

        if (contexto.getFerramentaAtual() instanceof FerramentaEditarVertice) {
            g.setFill(Color.RED);
            for (FormaGeometrica f : contexto.getSelecionados()) {
                if (f instanceof Circulo) continue;
                for (Ponto p : f.getPontos()) g.fillOval(p.getX() - 4, p.getY() - 4, 8, 8);
            }
        }

        double[] caixa = contexto.getCaixaSelecao();
        if (caixa != null) {
            double x = Math.min(caixa[0], caixa[2]), y = Math.min(caixa[1], caixa[3]);
            double cw = Math.abs(caixa[2] - caixa[0]), ch = Math.abs(caixa[3] - caixa[1]);
            g.setFill(Color.rgb(30, 144, 255, 0.15));
            g.fillRect(x, y, cw, ch);
            g.setStroke(Color.DODGERBLUE);
            g.setLineWidth(1);
            g.setLineDashes(5, 4);
            g.strokeRect(x, y, cw, ch);
            g.setLineDashes((double[]) null);
        }
    }

    private void desenharForma(FormaGeometrica f, boolean selecionada) {
        GraphicsContext g = areaDeDesenho.getGraphicsContext2D();
        Color preenchimento = Color.web(f.getCorPreenchimentoHex());
        Color borda = Color.web(f.getCorBordaHex());
        double espessura = f.getEspessuraBorda();
        g.setLineDashes((double[]) null);

        if (f instanceof Circulo && !((Circulo) f).isDeformado()) {
            Circulo c = (Circulo) f;
            Ponto ct = c.getCentroide();
            double r = c.getRaio();
            g.setFill(preenchimento);
            g.fillOval(ct.getX() - r, ct.getY() - r, 2 * r, 2 * r);
            g.setStroke(borda);
            g.setLineWidth(espessura);
            g.strokeOval(ct.getX() - r, ct.getY() - r, 2 * r, 2 * r);
            if (selecionada) {
                g.setStroke(COR_SELECAO);
                g.setLineWidth(espessura + 1.5);
                g.setLineDashes(6, 4);
                g.strokeOval(ct.getX() - r, ct.getY() - r, 2 * r, 2 * r);
                g.setLineDashes((double[]) null);
            }
            return;
        }

        List<Ponto> pts = (f instanceof Circulo) ? ((Circulo) f).getContorno(120) : f.getPontos();
        int n = pts.size();
        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = pts.get(i).getX();
            ys[i] = pts.get(i).getY();
        }
        if (n >= 3) {
            g.setFill(preenchimento);
            g.fillPolygon(xs, ys, n);
            g.setStroke(borda);
            g.setLineWidth(espessura);
            g.strokePolygon(xs, ys, n);
            if (selecionada) {
                g.setStroke(COR_SELECAO);
                g.setLineWidth(espessura + 1.5);
                g.setLineDashes(6, 4);
                g.strokePolygon(xs, ys, n);
                g.setLineDashes((double[]) null);
            }
        } else if (n >= 1) {
            g.setStroke(borda);
            g.setLineWidth(espessura);
            g.strokePolyline(xs, ys, n);
            g.setFill(borda);
            g.fillOval(xs[0] - 3, ys[0] - 3, 6, 6);
        }
    }

    // ------------------------------------------------------------ persistência
    private boolean salvarArquivo() {
        File destino = arquivoAtual;
        if (destino == null) {
            FileChooser fc = new FileChooser();
            fc.setTitle("Salvar desenho");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Desenho vetorial (*.json)", "*.json"));
            fc.setInitialFileName("desenho.json");
            destino = fc.showSaveDialog(stage);
            if (destino == null) return false;
            if (!destino.getName().toLowerCase().endsWith(".json")) {
                destino = new File(destino.getParentFile(), destino.getName() + ".json");
            }
        }
        try {
            PersistenciaService.salvar(destino, contexto.getFormas(),
                    areaDeDesenho.getWidth(), areaDeDesenho.getHeight(), contexto.isGridSnappingHabilitado());
            arquivoAtual = destino;
            contexto.setAlterado(false);
            return true;
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao salvar",
                    "Não foi possível salvar o arquivo:\n" + e.getMessage());
            return false;
        }
    }

    private void carregarArquivo() {
        if (!confirmarDescarte()) return;
        FileChooser fc = new FileChooser();
        fc.setTitle("Carregar desenho");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Desenho vetorial (*.json)", "*.json"));
        File arq = fc.showOpenDialog(stage);
        if (arq == null) return;
        try {
            PersistenciaService.Documento doc = PersistenciaService.carregar(arq);
            contexto.carregarDocumento(doc.getFormas(), doc.isGridSnapping());
            arquivoAtual = arq;
        } catch (ArquivoInvalidoException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Arquivo corrompido",
                    e.getMessage() + "\nO desenho atual foi mantido.");
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao carregar",
                    "Não foi possível ler o arquivo:\n" + e.getMessage());
        }
    }

    private void novoDocumento() {
        if (!confirmarDescarte()) return;
        contexto.novoDocumento();
        arquivoAtual = null;
        atualizarTitulo();
    }

    /** Retorna true se pode prosseguir (nada a salvar, salvou ou descartou). */
    private boolean confirmarDescarte() {
        if (!contexto.isAlterado()) return true;
        ButtonType salvar = new ButtonType("Salvar", ButtonBar.ButtonData.YES);
        ButtonType descartar = new ButtonType("Descartar", ButtonBar.ButtonData.NO);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Há alterações não salvas. Deseja salvá-las antes de continuar?", salvar, descartar, cancelar);
        a.setTitle("Alterações não salvas");
        a.setHeaderText("Alterações não salvas");
        Optional<ButtonType> r = a.showAndWait();
        if (r.isEmpty() || r.get() == cancelar) return false;
        if (r.get() == salvar) return salvarArquivo();
        return true;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert a = new Alert(tipo, mensagem);
        a.setTitle(titulo);
        a.setHeaderText(titulo);
        a.showAndWait();
    }

    private void atualizarTitulo() {
        if (stage == null) return;
        String nome = arquivoAtual == null ? "Sem título" : arquivoAtual.getName();
        stage.setTitle("Editor Vetorial - " + nome + (contexto.isAlterado() ? " *" : ""));
    }
}
