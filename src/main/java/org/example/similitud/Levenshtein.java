package org.example.similitud;

import org.example.modelo.Articulo;
import org.example.texto.Preprocesador;

import java.util.List;

/**
 * Distancia de Levenshtein a nivel de PALABRAS (programación dinámica).
 * d[i][j] = mínimo de ediciones (insertar, borrar, sustituir una palabra)
 * para convertir las primeras i palabras de A en las primeras j de B.
 * Tiempo O(n·m) y espacio O(n·m).
 */
public class Levenshtein implements Similitud {

    private final Preprocesador preprocesador = new Preprocesador();

    @Override
    public String nombre() {
        return "Levenshtein";
    }

    @Override
    public double calcular(Articulo a, Articulo b) {
        return calcular(preprocesador.tokenizar(a.resumen), preprocesador.tokenizar(b.resumen));
    }

    /** Similitud = 1 - distancia / max(n, m). */
    public double calcular(List<String> a, List<String> b) {
        int mayor = Math.max(a.size(), b.size());
        if (mayor == 0) {
            return 1.0;
        }
        return 1.0 - (double) distancia(a, b)[a.size()][b.size()] / mayor;
    }

    /** Llena la matriz DP completa. O(n·m). */
    public int[][] distancia(List<String> a, List<String> b) {
        int n = a.size();
        int m = b.size();
        int[][] d = new int[n + 1][m + 1];

        // Casos base: pasar de/hacia la lista vacía cuesta i (o j) ediciones
        for (int i = 0; i <= n; i++) {
            d[i][0] = i;
        }
        for (int j = 0; j <= m; j++) {
            d[0][j] = j;
        }

        // Recurrencia: min(borrar, insertar, sustituir/igualar)
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int costo = a.get(i - 1).equals(b.get(j - 1)) ? 0 : 1;
                int borrar = d[i - 1][j] + 1;
                int insertar = d[i][j - 1] + 1;
                int sustituir = d[i - 1][j - 1] + costo;
                d[i][j] = Math.min(borrar, Math.min(insertar, sustituir));
            }
        }
        return d;
    }

    /** Imprime la matriz DP llena, solo si las dos listas tienen 12 tokens o menos. */
    public void imprimirTraza(List<String> a, List<String> b) {
        if (a.size() > 12 || b.size() > 12) {
            System.out.println("(traza omitida: más de 12 tokens)");
            return;
        }
        int[][] d = distancia(a, b);
        System.out.println("Matriz de Levenshtein (filas = A, columnas = B)");
        System.out.printf("%-12s %-12s", "", "");
        for (String t : b) {
            System.out.printf("%-12s", t);
        }
        System.out.println();
        for (int i = 0; i <= a.size(); i++) {
            System.out.printf("%-12s", i == 0 ? "" : a.get(i - 1));
            for (int j = 0; j <= b.size(); j++) {
                System.out.printf("%-12d", d[i][j]);
            }
            System.out.println();
        }
        System.out.printf("Distancia = %d, similitud = %.4f%n",
                d[a.size()][b.size()], calcular(a, b));
    }
}
