package org.example.similitud;

import org.example.modelo.Articulo;
import org.example.texto.Preprocesador;

import java.util.List;

/**
 * Subsecuencia común más larga (LCS) a nivel de PALABRAS (programación dinámica).
 * c[i][j] = largo de la LCS de las primeras i palabras de A y las primeras j de B.
 * Tiempo O(n·m) y espacio O(n·m).
 */
public class SubsecuenciaComunMasLarga implements Similitud {

    private final Preprocesador preprocesador = new Preprocesador();

    @Override
    public String nombre() {
        return "LCS";
    }

    @Override
    public double calcular(Articulo a, Articulo b) {
        return calcular(preprocesador.tokenizar(a.resumen), preprocesador.tokenizar(b.resumen));
    }

    /** Similitud = 2·LCS / (n + m). */
    public double calcular(List<String> a, List<String> b) {
        int total = a.size() + b.size();
        if (total == 0) {
            return 1.0;
        }
        return 2.0 * matriz(a, b)[a.size()][b.size()] / total;
    }

    /** Llena la matriz DP completa. O(n·m). */
    public int[][] matriz(List<String> a, List<String> b) {
        int n = a.size();
        int m = b.size();
        int[][] c = new int[n + 1][m + 1]; // la fila 0 y la columna 0 quedan en 0 (casos base)

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (a.get(i - 1).equals(b.get(j - 1))) {
                    c[i][j] = c[i - 1][j - 1] + 1;           // coinciden: extiende la diagonal
                } else {
                    c[i][j] = Math.max(c[i - 1][j], c[i][j - 1]); // no: el mejor de arriba o izquierda
                }
            }
        }
        return c;
    }
}
