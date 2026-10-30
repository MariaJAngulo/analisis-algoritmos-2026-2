package org.example.busqueda;

import java.util.ArrayList;
import java.util.List;

/**
 * Knuth-Morris-Pratt.
 * Preprocesamiento: tabla de fallo (longitud del mayor prefijo del patrón que también es sufijo
 * de cada prefijo del patrón), O(m).
 * Búsqueda: recorre el texto UNA vez sin retroceder; ante un fallo, el patrón se desliza usando
 * la tabla en vez de volver atrás en el texto. O(n).
 */
public class Kmp implements BuscadorPatron {

    @Override
    public String nombre() {
        return "KMP";
    }

    /** fallo[i] = largo del mayor prefijo propio de p[0..i] que también es sufijo de p[0..i]. O(m). */
    public int[] construirFallo(char[] p) {
        int[] fallo = new int[p.length];
        int k = 0; // largo del prefijo-sufijo actual
        for (int i = 1; i < p.length; i++) {
            while (k > 0 && p[i] != p[k]) {
                k = fallo[k - 1]; // retrocede dentro del PATRÓN, no del texto
            }
            if (p[i] == p[k]) {
                k++;
            }
            fallo[i] = k;
        }
        return fallo;
    }

    @Override
    public ResultadoBusqueda buscar(char[] t, char[] p) {
        List<Integer> posiciones = new ArrayList<>();
        long comparaciones = 0;
        int n = t.length;
        int m = p.length;

        long inicioPre = System.nanoTime();
        int[] fallo = construirFallo(p);
        long preNs = System.nanoTime() - inicioPre;

        long inicioBusqueda = System.nanoTime();
        int j = 0; // cuántos caracteres del patrón coinciden ahora
        for (int i = 0; i < n; i++) {
            while (true) {
                comparaciones++;
                if (t[i] == p[j]) {
                    j++;
                    break;
                }
                if (j == 0) {
                    break;
                }
                j = fallo[j - 1]; // el patrón se desliza; el texto no retrocede
            }
            if (j == m) {
                posiciones.add(i - m + 1);
                j = fallo[j - 1]; // sigue buscando (permite coincidencias solapadas)
            }
        }
        long busquedaNs = System.nanoTime() - inicioBusqueda;

        return new ResultadoBusqueda(posiciones, comparaciones, 0, preNs, busquedaNs);
    }

    @Override
    public void imprimirTraza(String texto, String patron) {
        char[] p = patron.toCharArray();
        int[] fallo = construirFallo(p);
        System.out.println("--- KMP: tabla de fallo del patrón \"" + patron + "\" ---");
        System.out.print("i      ");
        for (int i = 0; i < p.length; i++) {
            System.out.printf("%3d", i);
        }
        System.out.print("\npatrón ");
        for (char c : p) {
            System.out.printf("%3c", c);
        }
        System.out.print("\nfallo  ");
        for (int f : fallo) {
            System.out.printf("%3d", f);
        }
        System.out.println();

        ResultadoBusqueda r = buscar(texto.toCharArray(), p);
        System.out.println("Coincidencias en posiciones " + r.posiciones
                + " con " + r.comparaciones + " comparaciones (texto de " + texto.length() + " caracteres).");
    }
}
