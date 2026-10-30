package org.example.busqueda;

import java.util.ArrayList;
import java.util.List;

/**
 * Rabin-Karp con hash rodante.
 * Preprocesamiento: hash del patrón y de la primera ventana del texto, O(m).
 * Búsqueda: desliza la ventana actualizando el hash en O(1):
 *     h = (d·(h - t[i]·dm) + t[i+m]) mod q,  con d = 256 y dm = d^(m-1) mod q.
 * Solo si el hash de la ventana coincide con el del patrón se verifica carácter por carácter.
 * Si el hash coincide pero el texto no, es una COLISIÓN. Promedio O(n + m); peor caso O(n·m).
 */
public class RabinKarp implements BuscadorPatron {

    public static final long PRIMO_GRANDE = 1_000_000_007L;
    public static final long PRIMO_TRAZA = 101L; // pequeño: provoca colisiones que se pueden mostrar
    private static final long BASE = 256;

    private final long primo;

    public RabinKarp() {
        this(PRIMO_GRANDE);
    }

    public RabinKarp(long primo) {
        this.primo = primo;
    }

    @Override
    public String nombre() {
        return "Rabin-Karp";
    }

    @Override
    public ResultadoBusqueda buscar(char[] t, char[] p) {
        return buscar(t, p, primo, false);
    }

    private ResultadoBusqueda buscar(char[] t, char[] p, long q, boolean traza) {
        List<Integer> posiciones = new ArrayList<>();
        long comparaciones = 0;
        long colisiones = 0;
        int n = t.length;
        int m = p.length;

        // ---- Preprocesamiento: dm, hash del patrón y hash de la primera ventana (O(m))
        long inicioPre = System.nanoTime();
        long dm = 1;
        for (int i = 0; i < m - 1; i++) {
            dm = (dm * BASE) % q;
        }
        long hashPatron = 0;
        long hashVentana = 0;
        for (int i = 0; i < m && i < n; i++) {
            hashPatron = (hashPatron * BASE + p[i]) % q;
            hashVentana = (hashVentana * BASE + t[i]) % q;
        }
        long preNs = System.nanoTime() - inicioPre;

        if (traza) {
            System.out.println("q = " + q + ", base d = " + BASE + ", dm = d^(m-1) mod q = " + dm
                    + ", hash del patrón = " + hashPatron + "\n");
        }

        // ---- Búsqueda
        long inicioBusqueda = System.nanoTime();
        for (int s = 0; s <= n - m; s++) {
            String estado = "distinto";
            if (hashVentana == hashPatron) {
                boolean igual = true;
                for (int j = 0; j < m; j++) { // verificación carácter por carácter
                    comparaciones++;
                    if (t[s + j] != p[j]) {
                        igual = false;
                        break;
                    }
                }
                if (igual) {
                    posiciones.add(s);
                    estado = "COINCIDENCIA";
                } else {
                    colisiones++;
                    estado = "COLISIÓN (mismo hash, texto distinto)";
                }
            }
            if (traza) {
                System.out.printf("s = %2d  ventana \"%s\"  hash = %3d  -> %s%n",
                        s, new String(t, s, m), hashVentana, estado);
            }
            if (s < n - m) {
                // Quita t[s], desplaza una posición y agrega t[s+m]; el "+ q" evita negativos
                long sinPrimero = (hashVentana - (t[s] * dm) % q + q) % q;
                hashVentana = (sinPrimero * BASE + t[s + m]) % q;
            }
        }
        long busquedaNs = System.nanoTime() - inicioBusqueda;

        return new ResultadoBusqueda(posiciones, comparaciones, colisiones, preNs, busquedaNs);
    }

    @Override
    public void imprimirTraza(String texto, String patron) {
        System.out.println("--- Rabin-Karp: hash de cada ventana (q = " + PRIMO_TRAZA + ") ---");
        ResultadoBusqueda r = buscar(texto.toCharArray(), patron.toCharArray(), PRIMO_TRAZA, true);
        System.out.println("\nCoincidencias en " + r.posiciones + ", " + r.colisiones + " colisiones, "
                + r.comparaciones + " comparaciones de verificación.");
    }
}
