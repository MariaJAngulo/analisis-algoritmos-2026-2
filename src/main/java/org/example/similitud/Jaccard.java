package org.example.similitud;

import org.example.modelo.Articulo;
import org.example.texto.Preprocesador;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Similitud de Jaccard entre los CONJUNTOS de palabras de dos abstracts.
 * J(A,B) = |A ∩ B| / |A ∪ B|.
 * Tiempo O(n + m): armar los dos conjuntos con HashSet.
 */
public class Jaccard implements Similitud {

    private final Preprocesador preprocesador = new Preprocesador();

    @Override
    public String nombre() {
        return "Jaccard";
    }

    @Override
    public double calcular(Articulo a, Articulo b) {
        return calcular(preprocesador.tokenizar(a.resumen), preprocesador.tokenizar(b.resumen));
    }

    public double calcular(List<String> a, List<String> b) {
        Set<String> interseccion = new HashSet<>(a);
        interseccion.retainAll(b);          // solo las palabras que están en A y en B
        Set<String> union = new HashSet<>(a);
        union.addAll(b);                    // las palabras que están en A o en B
        if (union.isEmpty()) {
            return 1.0;
        }
        return (double) interseccion.size() / union.size();
    }
}
