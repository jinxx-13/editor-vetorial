package org.example.Views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.example.Controller.EditorContext;
import org.example.Controller.EditorContext.TipoForma;
import org.example.Controller.Ferramenta;
import org.example.Controller.FerramentaDesenhar;
import org.example.Controller.FerramentaEditarVertice;
import org.example.Controller.FerramentaExcluir;
import org.example.Controller.FerramentaInserirForma;
import org.example.Controller.FerramentaSelecionar;
import org.example.Controller.FerramentaTransformar;
import org.example.Controller.ModoFerramenta;
import org.example.Models.FormaGeometrica;

/** Barra lateral: estilos, ferramentas, formas, camadas/grupos e sistema. */
public class BarraLateralView extends VBox {

    private final EditorContext contexto;
    private final Runnable aoSalvar;
    private final Runnable aoCarregar;
    private final Runnable aoNovo;

    private final ColorPicker cpPreenchimento = new ColorPicker(Color.web("#D3D3D3"));
    private final ColorPicker cpBorda = new ColorPicker(Color.web("#000000"));
    private final Slider slEspessura = new Slider(1, 10, 1);
    private final CheckBox cbGrade = new CheckBox("Grade Magnética (Snapping)");
    private final ToggleGroup grupoFerramentas = new ToggleGroup();
    private boolean atualizando;

    public BarraLateralView(EditorContext contexto, Runnable aoSalvar, Runnable aoCarregar, Runnable aoNovo) {
        this.contexto = contexto;
        this.aoSalvar = aoSalvar;
        this.aoCarregar = aoCarregar;
        this.aoNovo = aoNovo;

        setSpacing(5);
        setPadding(new Insets(8));
        setPrefWidth(185);
        setMinWidth(185);
        setStyle("-fx-background-color: #f0f0f0;");

        grupoFerramentas.selectedToggleProperty().addListener((o, antigo, novo) -> {
            if (novo == null && antigo != null) antigo.setSelected(true);
        });

        construirSecaoEstilos();
        construirSecaoFerramentas();
        construirSecaoFormas();
        construirSecaoCamadas();
        construirSecaoSistema();
    }

    // ------------------------------------------------------------ seções
    private void construirSecaoEstilos() {
        getChildren().add(titulo("--- ESTILOS ---"));
        cpPreenchimento.setMaxWidth(Double.MAX_VALUE);
        cpBorda.setMaxWidth(Double.MAX_VALUE);

        cpPreenchimento.valueProperty().addListener((o, a, n) -> {
            if (atualizando) return;
            String hex = paraHex(n);
            contexto.setCorPreenchimentoAtual(hex);
            contexto.aplicarCorPreenchimentoSelecao(hex);
        });
        cpBorda.valueProperty().addListener((o, a, n) -> {
            if (atualizando) return;
            String hex = paraHex(n);
            contexto.setCorBordaAtual(hex);
            contexto.aplicarCorBordaSelecao(hex);
        });

        slEspessura.setMajorTickUnit(1);
        slEspessura.setMinorTickCount(0);
        slEspessura.setSnapToTicks(true);
        slEspessura.valueChangingProperty().addListener((o, a, mudando) -> {
            if (mudando && contexto.temSelecao()) contexto.salvarEstado();
        });
        slEspessura.valueProperty().addListener((o, a, n) -> {
            if (atualizando) return;
            contexto.setEspessuraAtual(n.doubleValue());
            contexto.aplicarEspessuraSelecao(n.doubleValue(), !slEspessura.isValueChanging());
        });

        cbGrade.selectedProperty().addListener((o, a, n) -> {
            if (atualizando) return;
            contexto.setGridSnappingHabilitado(n);
        });

        getChildren().addAll(new Label("Cor Preenchimento:"), cpPreenchimento,
                new Label("Cor Borda:"), cpBorda,
                new Label("Espessura da Borda:"), slEspessura, cbGrade);
    }

    private void construirSecaoFerramentas() {
        getChildren().add(titulo("--- FERRAMENTAS ---"));
        ToggleButton selecionar = criarToggle("Selecionar", new FerramentaSelecionar(), null);
        getChildren().addAll(
                criarToggle("Polígono (cliques)", new FerramentaDesenhar(), null),
                selecionar,
                criarToggle("Escalar", new FerramentaTransformar(ModoFerramenta.ESCALAR), null),
                criarToggle("Rotacionar", new FerramentaTransformar(ModoFerramenta.ROTACIONAR), null),
                criarToggle("Cisalhar", new FerramentaTransformar(ModoFerramenta.CISALHAR), null),
                criarToggle("Excluir", new FerramentaExcluir(), null),
                criarToggle("Editar Vértice", new FerramentaEditarVertice(), null),
                botao("Excluir Selecionados", contexto::excluirSelecionados));
        selecionar.setSelected(true);
        contexto.setFerramentaAtual(new FerramentaSelecionar());
    }

    private void construirSecaoFormas() {
        getChildren().add(titulo("--- FORMAS ---"));
        getChildren().addAll(
                criarToggle("Inserir Quadrado", new FerramentaInserirForma(), () -> contexto.setTipoInsercao(TipoForma.QUADRADO)),
                criarToggle("Inserir Círculo", new FerramentaInserirForma(), () -> contexto.setTipoInsercao(TipoForma.CIRCULO)),
                criarToggle("Inserir Hexágono", new FerramentaInserirForma(), () -> contexto.setTipoInsercao(TipoForma.HEXAGONO)));
    }

    private void construirSecaoCamadas() {
        getChildren().add(titulo("--- CAMADAS/GRUPO ---"));
        getChildren().addAll(
                botao("Trazer p/ Frente", contexto::trazerParaFrente),
                botao("Enviar p/ Trás", contexto::enviarParaTras),
                botao("Agrupar", contexto::agruparSelecionados),
                botao("Desagrupar", contexto::desagruparSelecionados));
    }

    private void construirSecaoSistema() {
        getChildren().add(titulo("--- SISTEMA ---"));
        getChildren().addAll(
                botao("Desfazer", contexto::desfazer),
                botao("Refazer", contexto::refazer),
                botao("Novo", aoNovo),
                botao("Salvar", aoSalvar),
                botao("Carregar", aoCarregar));
    }

    // ------------------------------------------------------------ sincronização
    /** Reflete na interface as cores/espessura da seleção (ou do estilo atual). */
    public void atualizarCoresInterface() {
        atualizando = true;
        try {
            FormaGeometrica f = contexto.getPrimeiraSelecionada();
            String fill = f != null ? f.getCorPreenchimentoHex() : contexto.getCorPreenchimentoAtual();
            String borda = f != null ? f.getCorBordaHex() : contexto.getCorBordaAtual();
            double esp = f != null ? f.getEspessuraBorda() : contexto.getEspessuraAtual();

            Color cf = Color.web(fill);
            if (!cf.equals(cpPreenchimento.getValue())) cpPreenchimento.setValue(cf);
            Color cb = Color.web(borda);
            if (!cb.equals(cpBorda.getValue())) cpBorda.setValue(cb);
            if (!slEspessura.isValueChanging() && Math.abs(slEspessura.getValue() - esp) > 1e-9) {
                slEspessura.setValue(Math.max(1, Math.min(10, esp)));
            }
            if (cbGrade.isSelected() != contexto.isGridSnappingHabilitado()) {
                cbGrade.setSelected(contexto.isGridSnappingHabilitado());
            }
        } finally {
            atualizando = false;
        }
    }

    // ------------------------------------------------------------ auxiliares
    private Label titulo(String texto) {
        Label l = new Label(texto);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER);
        l.setStyle("-fx-font-size: 10px; -fx-text-fill: #555555; -fx-padding: 6 0 0 0;");
        return l;
    }

    private ToggleButton criarToggle(String texto, Ferramenta ferramenta, Runnable antes) {
        ToggleButton tb = new ToggleButton(texto);
        tb.setMaxWidth(Double.MAX_VALUE);
        tb.setToggleGroup(grupoFerramentas);
        tb.setOnAction(e -> {
            if (!tb.isSelected()) return;
            if (antes != null) antes.run();
            contexto.setFerramentaAtual(ferramenta);
        });
        return tb;
    }

    private Button botao(String texto, Runnable acao) {
        Button b = new Button(texto);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> acao.run());
        return b;
    }

    private static String paraHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int) Math.round(c.getRed() * 255),
                (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue() * 255));
    }
}
