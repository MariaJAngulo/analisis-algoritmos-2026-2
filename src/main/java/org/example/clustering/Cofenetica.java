package org.example.clustering;

/**
 * Métrica interna: coeficiente de correlación cofenética.
 * La distancia cofenética entre i y j es la altura de la fusión donde quedan juntos por primera vez.
 * Se calcula la correlación de Pearson entre las n(n-1)/2 distancias originales y las cofenéticas:
 * mientras más cerca de 1, mejor conserva el dendrograma las distancias reales.
 */
public class Cofenetica {

    public static double correlacion(MatrizDistancias matriz, Nodo raiz) {
        int n = matriz.tamano();
        double[][] cofenetica = new double[n][n];
        llenar(raiz, cofenetica);

        int pares = n * (n - 1) / 2;
        double[] originales = new double[pares];
        double[] arbol = new double[pares];
        int k = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                originales[k] = matriz.get(i, j);
                arbol[k] = cofenetica[i][j];
                k++;
            }
        }
        return pearson(originales, arbol);
    }

    // Cada nodo interno une dos grupos: todos los pares entre ellos quedan a la altura del nodo
    private static void llenar(Nodo nodo, double[][] cofenetica) {
        if (nodo.esHoja()) {
            return;
        }
        for (int a : nodo.izquierdo.hojas) {
            for (int b : nodo.derecho.hojas) {
                cofenetica[a][b] = nodo.altura;
                cofenetica[b][a] = nodo.altura;
            }
        }
        llenar(nodo.izquierdo, cofenetica);
        llenar(nodo.derecho, cofenetica);
    }

    /** r = Σ(x-x̄)(y-ȳ) / (√Σ(x-x̄)² · √Σ(y-ȳ)²). O(p). */
    public static double pearson(double[] x, double[] y) {
        int p = x.length;
        double mediaX = 0;
        double mediaY = 0;
        for (int i = 0; i < p; i++) {
            mediaX += x[i];
            mediaY += y[i];
        }
        mediaX /= p;
        mediaY /= p;

        double covarianza = 0;
        double varianzaX = 0;
        double varianzaY = 0;
        for (int i = 0; i < p; i++) {
            covarianza += (x[i] - mediaX) * (y[i] - mediaY);
            varianzaX += (x[i] - mediaX) * (x[i] - mediaX);
            varianzaY += (y[i] - mediaY) * (y[i] - mediaY);
        }
        if (varianzaX == 0 || varianzaY == 0) {
            return 0.0;
        }
        return covarianza / (Math.sqrt(varianzaX) * Math.sqrt(varianzaY));
    }
}
