package org.example.similitud;

import org.example.modelo.Articulo;
import org.example.texto.Preprocesador;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Similitud de Jaccard entre los CONJUNTOS de palabras de dos abstracts.
 * J(A,B) = |A ∩ B| / |A ∪ B|, con |A ∪ B| = |A| + |B| - |A ∩ B|.
 * Tiempo O(n + m): armar los conjuntos y recorrer el más pequeño.
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
        Set<String> conjuntoA = new HashSet<>(a);
        Set<String> conjuntoB = new HashSet<>(b);
        int interseccion = contarInterseccion(conjuntoA, conjuntoB);
        int union = conjuntoA.size() + conjuntoB.size() - interseccion;
        if (union == 0) {
            return 1.0;
        }
        return (double) interseccion / union;
    }

    /** Cuenta los elementos comunes recorriendo el conjunto más pequeño. O(min(n, m)). */
    private int contarInterseccion(Set<String> x, Set<String> y) {
        Set<String> menor = x.size() <= y.size() ? x : y;
        Set<String> mayor = menor == x ? y : x;
        int cuenta = 0;
        for (String palabra : menor) {
            if (mayor.contains(palabra)) {
                cuenta++;
            }
        }
        return cuenta;
    }

    /** Imprime los conjuntos y su intersección, solo si las listas tienen 12 tokens o menos. */
    public void imprimirTraza(List<String> a, List<String> b) {
        if (a.size() > 12 || b.size() > 12) {
            System.out.println("(traza omitida: más de 12 tokens)");
            return;
        }
        Set<String> conjuntoA = new HashSet<>(a);
        Set<String> conjuntoB = new HashSet<>(b);
        Set<String> menor = conjuntoA.size() <= conjuntoB.size() ? conjuntoA : conjuntoB;
        Set<String> mayor = menor == conjuntoA ? conjuntoB : conjuntoA;

        StringBuilder comunes = new StringBuilder();
        int interseccion = 0;
        for (String palabra : menor) {
            if (mayor.contains(palabra)) {
                interseccion++;
                comunes.append(palabra).append(" ");
            }
        }
        int union = conjuntoA.size() + conjuntoB.size() - interseccion;

        System.out.println("A = " + conjuntoA + "  |A| = " + conjuntoA.size());
        System.out.println("B = " + conjuntoB + "  |B| = " + conjuntoB.size());
        System.out.println("A ∩ B = {" + comunes.toString().trim() + "}  |A ∩ B| = " + interseccion);
        System.out.println("|A ∪ B| = " + conjuntoA.size() + " + " + conjuntoB.size()
                + " - " + interseccion + " = " + union);
        System.out.printf("Jaccard = %d / %d = %.4f%n", interseccion, union,
                union == 0 ? 1.0 : (double) interseccion / union);
    }
}
