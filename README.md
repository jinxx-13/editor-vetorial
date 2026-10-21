# Editor de Desenho Vetorial Interativo (JavaFX)

Projeto N3 - Programação Orientada a Objetos 1 - UniRV.

## Requisitos
- JDK 17 ou superior
- Apache Maven 3.8+

## Executar
```
mvn clean javafx:run
```

## Compilar
```
mvn clean package
```

## Arquitetura (MVC)
- `org.example.Models`: `FormaGeometrica`, `FormaBase`, `Ponto`, `Poligono`, `Circulo`, `Quadrado`, `Hexagono`
- `org.example.Controller`: `EditorContext`, `HistoricoController`, `Ferramenta` e ferramentas, `PersistenciaService`
- `org.example.Views`: `ContainerApp`, `BarraLateralView`, `PainelStatusView`

## Padrões de projeto
- **State**: `EditorContext` (contexto) + `Ferramenta` (estado) + ferramentas concretas
- **Prototype**: `FormaGeometrica.clonar()` (cópia profunda)
- **Memento**: `HistoricoController` guarda cópias profundas da lista de formas (desfazer/refazer)
- **Composite**: formas com o mesmo `groupId` são tratadas como uma única forma lógica (seleção, translação, escala, rotação e cisalhamento)

## Uso
- **Polígono**: clique para adicionar vértices; duplo clique, botão direito, Enter ou clique no primeiro ponto fecham (mínimo de 3 pontos). Esc cancela.
- **Inserir Quadrado/Círculo/Hexágono**: clique (tamanho padrão) ou arraste (define o raio).
- **Selecionar**: clique, Shift+clique ou arraste uma caixa. Arrastar uma forma selecionada a move.
- **Escalar/Rotacionar/Cisalhar**: selecione e arraste com a ferramenta ativa.
- **Editar Vértice**: arraste os pontos vermelhos.
- Atalhos: Ctrl+Z desfazer, Ctrl+Y refazer, Delete exclui, Ctrl+S salva.
- Persistência em JSON (Gson). Arquivo corrompido gera alerta sem fechar o programa; alterações não salvas são avisadas ao fechar.
