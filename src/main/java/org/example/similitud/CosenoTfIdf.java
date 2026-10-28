package org.example.similitud;

import org.example.modelo.Articulo;
import org.example.texto.Preprocesador;

import java.util.HashMap;
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
            for (String termino : new java.util.HashSet<>(preprocesador.tokenizar(articulo.resumen))) {
                df.merge(termino, 1, Integer::sum);
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
            frecuencia.merge(t, 1, Integer::sum);
        }
        Map<String, Double> vector = new HashMap<>();
        for (Map.Entry<String, Integer> e : frecuencia.entrySet()) {
            double tf = (double) e.getValue() / tokens.size();
            vector.put(e.getKey(), tf * idf(e.getKey()));
        }
        return vector;
    }

    /**
     * Coseno entre dos vectores dispersos. El producto punto solo recorre los términos
     * comunes (recorriendo el vector más corto). Si algún vector es nulo, devuelve 0.
     */
    public double coseno(Map<String, Double> a, Map<String, Double> b) {
        Map<String, Double> menor = a.size() <= b.size() ? a : b;
        Map<String, Double> mayor = menor == a ? b : a;

        double producto = 0;
        for (Map.Entry<String, Double> e : menor.entrySet()) {
            Double otro = mayor.get(e.getKey());
            if (otro != null) {
                producto += e.getValue() * otro;
            }
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

    /** Imprime tf, idf y pesos de cada término y el coseno. Solo con 12 tokens o menos. */
    public void imprimirTraza(List<String> a, List<String> b) {
        if (a.size() > 12 || b.size() > 12) {
            System.out.println("(traza omitida: más de 12 tokens)");
            return;
        }
        Map<String, Double> va = vectorTfIdf(a);
        Map<String, Double> vb = vectorTfIdf(b);

        java.util.TreeSet<String> terminos = new java.util.TreeSet<>(va.keySet());
        terminos.addAll(vb.keySet());

        System.out.printf("%-14s %8s %8s %8s %8s %8s%n", "término", "idf", "tf A", "tf B", "peso A", "peso B");
        double producto = 0;
        for (String t : terminos) {
            double pa = va.getOrDefault(t, 0.0);
            double pb = vb.getOrDefault(t, 0.0);
            double tfA = (double) java.util.Collections.frequency(a, t) / a.size();
            double tfB = (double) java.util.Collections.frequency(b, t) / b.size();
            System.out.printf("%-14s %8.4f %8.4f %8.4f %8.4f %8.4f%n", t, idf(t), tfA, tfB, pa, pb);
            producto += pa * pb;
        }
        double na = norma(va);
        double nb = norma(vb);
        double cos = coseno(va, vb);
        System.out.printf("Producto punto = %.6f, |A| = %.6f, |B| = %.6f%n", producto, na, nb);
        System.out.printf("Coseno = %.4f, ángulo = %.2f°%n", cos,
                Math.toDegrees(Math.acos(Math.min(1.0, Math.max(-1.0, cos)))));
    }
}
