package org.example.similitud;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.modelo.Articulo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Modelo B: Sentence-BERT all-MiniLM-L6-v2 (embeddings de frase, contextuales, 384 dimensiones).
 * El modelo ya codificó cada abstract completo (scripts/generar_embeddings.py);
 * aquí solo se cargan esos vectores y se compara con coseno.
 */
public class EmbeddingMiniLM extends Embedding {

    private final Map<String, double[]> vectores = new HashMap<>();

    /** Carga datos/embeddings_minilm.json con la forma { "id": [384 números] }. */
    public EmbeddingMiniLM(String ruta) throws IOException {
        JsonNode raiz = new ObjectMapper().readTree(new File(ruta));
        var campos = raiz.fields();
        while (campos.hasNext()) {
            Map.Entry<String, JsonNode> campo = campos.next();
            JsonNode numeros = campo.getValue();
            double[] vector = new double[numeros.size()];
            for (int i = 0; i < vector.length; i++) {
                vector[i] = numeros.get(i).asDouble();
            }
            vectores.put(campo.getKey(), vector);
        }
    }

    @Override
    public String nombre() {
        return "MiniLM (SBERT)";
    }

    @Override
    public double[] vectorDe(Articulo articulo) {
        double[] vector = vectores.get(articulo.id);
        if (vector == null) {
            throw new IllegalStateException("No hay vector MiniLM para el artículo " + articulo.id
                    + ". Vuelve a ejecutar scripts/generar_embeddings.py");
        }
        return vector;
    }

    public int dimension() {
        return vectores.isEmpty() ? 0 : vectores.values().iterator().next().length;
    }
}