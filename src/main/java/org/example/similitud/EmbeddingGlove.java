package org.example.similitud;

import org.example.modelo.Articulo;
import org.example.texto.Preprocesador;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modelo A: GloVe (embeddings de palabras, estáticos, 100 dimensiones).
 * Cada palabra tiene UN vector fijo; el vector del abstract es el PROMEDIO
 * de los vectores de sus tokens. Las palabras que no están en el diccionario se ignoran.
 * Los vectores los genera una sola vez scripts/generar_embeddings.py.
 * Tiempo por abstract: O(n·d), n = tokens, d = dimensión.
 */
public class EmbeddingGlove extends Embedding {

    private final Map<String, double[]> diccionario = new HashMap<>();
    private final Map<String, double[]> cache = new HashMap<>(); // vector por id de artículo
    private final Preprocesador preprocesador = new Preprocesador();
    private int dimension = 0;

    /** Carga datos/glove_corpus.txt: una línea por palabra, "palabra v1 v2 ... v100". */
    public EmbeddingGlove(String ruta) throws IOException {
        try (BufferedReader lector = Files.newBufferedReader(Paths.get(ruta), StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                String[] partes = linea.split(" ");
                if (partes.length < 2) {
                    continue;
                }
                double[] vector = new double[partes.length - 1];
                for (int i = 1; i < partes.length; i++) {
                    vector[i - 1] = Double.parseDouble(partes[i]);
                }
                dimension = vector.length;
                diccionario.put(partes[0], vector);
            }
        }
    }

    @Override
    public String nombre() {
        return "GloVe (promedio)";
    }

    @Override
    public double[] vectorDe(Articulo articulo) {
        double[] vector = cache.get(articulo.id);
        if (vector == null) {
            vector = promedio(preprocesador.tokenizar(articulo.resumen));
            cache.put(articulo.id, vector);
        }
        return vector;
    }

    /** Promedio de los vectores de los tokens conocidos. Si no hay ninguno, vector de ceros. */
    public double[] promedio(List<String> tokens) {
        double[] suma = new double[dimension];
        int conocidos = 0;
        for (String token : tokens) {
            double[] v = diccionario.get(token);
            if (v == null) {
                continue; // palabra fuera del diccionario: se ignora
            }
            for (int i = 0; i < dimension; i++) {
                suma[i] += v[i];
            }
            conocidos++;
        }
        if (conocidos > 0) {
            for (int i = 0; i < dimension; i++) {
                suma[i] /= conocidos;
            }
        }
        return suma;
    }

    public int palabrasEnDiccionario() {
        return diccionario.size();
    }

    public int dimension() {
        return dimension;
    }
}