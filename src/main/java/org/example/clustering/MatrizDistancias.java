package org.example.clustering;

import org.example.modelo.Articulo;
import org.example.similitud.CosenoTfIdf;

import java.util.List;

/**
 * Matriz n×n de distancias entre los abstracts: distancia = 1 - coseno TF-IDF
 * (reutiliza la clase del R1). Diagonal en 0 y matriz simétrica. Se arma en O(n²·costo del coseno).
 */
public class MatrizDistancias {

    private final List<Articulo> articulos;
    private final double[][] d;

    public MatrizDistancias(List<Articulo> articulos, CosenoTfIdf coseno) {
        this.articulos = articulos;
        int n = articulos.size();
        d = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double distancia = 1.0 - coseno.calcular(articulos.get(i), articulos.get(j));
                d[i][j] = Math.max(0.0, distancia); // evita -0.0000001 por redondeo
                d[j][i] = d[i][j];
            }
        }
    }

    public double get(int i, int j) {
        return d[i][j];
    }

    public int tamano() {
        return articulos.size();
    }

    public List<Articulo> articulos() {
        return articulos;
    }
}
