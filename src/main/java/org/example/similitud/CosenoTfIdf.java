package org.example.similitud;

import org.example.modelo.Articulo;
import org.example.texto.Preprocesador;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Similitud coseno sobre vectores TF-IDF (modelo de espacio vectorial).
 *   tf(t,d)  = frecuencia de t en d / total de tokens de d
 *   idf(t)   = ln(N / df(t)),  N = artículos del corpus, df = artículos que contienen t
 *   peso     = tf · idf
 *   coseno   = Σ(a_i·b_i) / (√Σa_i² · √Σb_i²)
 * Construir el idf cuesta O(L) (L = total de tokens del corpus); cada coseno O(n + m).
 * Esta misma clase la reutiliza el clustering (R2).
 */
public class CosenoTfIdf implements Similitud {

    private final Preprocesador preprocesador = new Preprocesador();
    private final Map<String, Double> idf = new HashMap<>();
    private final Map<String, Map<String, Double>> vectores = new HashMap<>(); // por id de artículo
    private final int totalArticulos;

    /** Recibe el corpus completo para calcular el idf de cada término. */
    public CosenoTfIdf(List<Articulo> corpus) {
        totalArticulos = corpus.size();

        // df(t): en cuántos abstracts aparece cada término (una vez por abstract)
        Map<String, Integer> df = new HashMap<>();
        for (Articulo articulo : corpus) {
            for (String termino : new HashSet<>(preprocesador.tokenizar(articulo.resumen))) {
                df.put(termino, df.getOrDefault(termino, 0) + 1);
            }
        }
        for (Map.Entry<String, Integer> e : df.entrySet()) {
            idf.put(e.getKey(), Math.log((double) totalArticulos / e.getValue()));
        }
    }

    @Override
    public String nombre() {
        return "Coseno TF-IDF";
    }

    @Override
    public double calcular(Articulo a, Articulo b) {
        return coseno(vectorDe(a), vectorDe(b));
    }

    /** idf de un término; si no está en el corpus se trata como df = 1. */
    public double idf(String termino) {
        Double valor = idf.get(termino);
        return valor != null ? valor : Math.log((double) totalArticulos);
    }

    /** Vector TF-IDF del abstract (término → peso), guardado para no recalcularlo. */
    public Map<String, Double> vectorDe(Articulo articulo) {
        Map<String, Double> vector = vectores.get(articulo.id);
        if (vector == null) {
            vector = vectorTfIdf(preprocesador.tokenizar(articulo.resumen));
            vectores.put(articulo.id, vector);
        }
        return vector;
    }

    /** Convierte una lista de tokens en su vector TF-IDF. O(n). */
    public Map<String, Double> vectorTfIdf(List<String> tokens) {
        Map<String, Integer> frecuencia = new HashMap<>();
        for (String t : tokens) {
            frecuencia.put(t, frecuencia.getOrDefault(t, 0) + 1);
        }
        Map<String, Double> vector = new HashMap<>();
        for (Map.Entry<String, Integer> e : frecuencia.entrySet()) {
            double tf = (double) e.getValue() / tokens.size();
            vector.put(e.getKey(), tf * idf(e.getKey()));
        }
        return vector;
    }

    /**
     * Coseno entre dos vectores guardados como mapas (término -> peso).
     * Producto punto: para cada término de A se busca su peso en B (0 si no está).
     * Si algún vector es nulo, devuelve 0.
     */
    public double coseno(Map<String, Double> a, Map<String, Double> b) {
        double producto = 0;
        for (String termino : a.keySet()) {
            producto += a.get(termino) * b.getOrDefault(termino, 0.0);
        }
        double normaA = norma(a);
        double normaB = norma(b);
        if (normaA == 0 || normaB == 0) {
            return 0.0;
        }
        return producto / (normaA * normaB);
    }

    private double norma(Map<String, Double> v) {
        double suma = 0;
        for (double peso : v.values()) {
            suma += peso * peso;
        }
        return Math.sqrt(suma);
    }
}
