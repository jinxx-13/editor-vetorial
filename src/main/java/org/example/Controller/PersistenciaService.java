package org.example.Controller;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.Models.Circulo;
import org.example.Models.FormaBase;
import org.example.Models.FormaGeometrica;
import org.example.Models.Hexagono;
import org.example.Models.Poligono;
import org.example.Models.Ponto;
import org.example.Models.Quadrado;

import java.io.File;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/** Persistência em JSON: configurações do canvas + lista de formas. */
public final class PersistenciaService {

    private static final int VERSAO = 1;

    private PersistenciaService() {
    }

    /** Resultado de uma leitura. */
    public static final class Documento {
        private final List<FormaGeometrica> formas;
        private final double largura;
        private final double altura;
        private final boolean gridSnapping;

        public Documento(List<FormaGeometrica> formas, double largura, double altura, boolean gridSnapping) {
            this.formas = formas;
            this.largura = largura;
            this.altura = altura;
            this.gridSnapping = gridSnapping;
        }

        public List<FormaGeometrica> getFormas() { return formas; }
        public double getLargura() { return largura; }
        public double getAltura() { return altura; }
        public boolean isGridSnapping() { return gridSnapping; }
    }

    // ------------------------------------------------------------ salvar
    public static void salvar(File arquivo, List<FormaGeometrica> formas,
                              double largura, double altura, boolean grid) throws IOException {
        JsonObject raiz = new JsonObject();
        raiz.addProperty("versao", VERSAO);

        JsonObject canvas = new JsonObject();
        canvas.addProperty("largura", largura);
        canvas.addProperty("altura", altura);
        canvas.addProperty("gridSnapping", grid);
        raiz.add("canvas", canvas);

        JsonArray arr = new JsonArray();
        for (FormaGeometrica f : formas) arr.add(paraJson(f));
        raiz.add("formas", arr);

        String json = new GsonBuilder().setPrettyPrinting().create().toJson(raiz);
        Files.writeString(arquivo.toPath(), json, StandardCharsets.UTF_8);
    }

    private static JsonObject paraJson(FormaGeometrica f) {
        JsonObject o = new JsonObject();
        String tipo = "POLIGONO";
        if (f instanceof Circulo) tipo = "CIRCULO";
        else if (f instanceof Quadrado) tipo = "QUADRADO";
        else if (f instanceof Hexagono) tipo = "HEXAGONO";
        o.addProperty("tipo", tipo);

        JsonArray pts = new JsonArray();
        for (Ponto p : f.getPontos()) {
            JsonArray par = new JsonArray();
            par.add(p.getX());
            par.add(p.getY());
            pts.add(par);
        }
        o.add("pontos", pts);
        if (f instanceof Circulo) o.addProperty("raio", ((Circulo) f).getRaio());
        o.addProperty("corPreenchimento", f.getCorPreenchimentoHex());
        o.addProperty("corBorda", f.getCorBordaHex());
        o.addProperty("espessura", f.getEspessuraBorda());
        if (f.getGroupId() != null) o.addProperty("groupId", f.getGroupId());
        return o;
    }

    // ------------------------------------------------------------ carregar
    public static Documento carregar(File arquivo) throws IOException, ArquivoInvalidoException {
        String texto;
        try {
            texto = Files.readString(arquivo.toPath(), StandardCharsets.UTF_8);
        } catch (CharacterCodingException e) {
            throw new ArquivoInvalidoException("O arquivo contém caracteres inválidos.", e);
        }
        try {
            JsonObject raiz = JsonParser.parseString(texto).getAsJsonObject();
            JsonObject canvas = raiz.getAsJsonObject("canvas");
            JsonArray arr = raiz.getAsJsonArray("formas");
            if (canvas == null || arr == null) {
                throw new ArquivoInvalidoException("Estrutura do arquivo incompleta (faltam 'canvas' ou 'formas').");
            }
            List<FormaGeometrica> formas = new ArrayList<>();
            for (JsonElement el : arr) formas.add(deJson(el.getAsJsonObject()));
            return new Documento(formas,
                    canvas.get("largura").getAsDouble(),
                    canvas.get("altura").getAsDouble(),
                    canvas.get("gridSnapping").getAsBoolean());
        } catch (RuntimeException e) {
            throw new ArquivoInvalidoException("O arquivo está corrompido ou não é um desenho válido.", e);
        }
    }

    private static FormaGeometrica deJson(JsonObject o) {
        String tipo = o.get("tipo").getAsString();
        List<Ponto> pts = new ArrayList<>();
        for (JsonElement el : o.getAsJsonArray("pontos")) {
            JsonArray par = el.getAsJsonArray();
            pts.add(new Ponto(par.get(0).getAsDouble(), par.get(1).getAsDouble()));
        }
        FormaBase f;
        switch (tipo) {
            case "CIRCULO":
                if (pts.size() != 1) throw new IllegalArgumentException("Círculo deve ter 1 ponto.");
                f = new Circulo(pts.get(0), o.get("raio").getAsDouble());
                break;
            case "QUADRADO":
                if (pts.size() != 4) throw new IllegalArgumentException("Quadrado deve ter 4 pontos.");
                f = new Quadrado(new Ponto(0, 0), 1);
                f.setPontos(pts);
                break;
            case "HEXAGONO":
                if (pts.size() != 6) throw new IllegalArgumentException("Hexágono deve ter 6 pontos.");
                f = new Hexagono(new Ponto(0, 0), 1);
                f.setPontos(pts);
                break;
            case "POLIGONO":
                Poligono p = new Poligono();
                p.setPontos(pts);
                p.validar();
                f = p;
                break;
            default:
                throw new IllegalArgumentException("Tipo de forma desconhecido: " + tipo);
        }
        f.setCorPreenchimentoHex(o.get("corPreenchimento").getAsString());
        f.setCorBordaHex(o.get("corBorda").getAsString());
        f.setEspessuraBorda(o.get("espessura").getAsDouble());
        if (o.has("groupId") && !o.get("groupId").isJsonNull()) {
            f.setGroupId(o.get("groupId").getAsString());
        }
        return f;
    }
}
