package org.example.busqueda;

import java.util.ArrayList;
import java.util.List;

/**
 * Boyer-Moore con la regla del CARÁCTER MALO (el enunciado pide "y/o": basta una regla).
 * Preprocesamiento: tabla ultima[c] = última posición de c en el patrón (-1 si no está), O(m + σ),
 * con σ = 256 caracteres.
 * Búsqueda: alinea el patrón en el texto y compara de DERECHA a IZQUIERDA. Si falla en la posición j
 * con el carácter c del texto, desplaza max(1, j - ultima[c]) lugares. Mejor caso O(n/m); peor O(n·m).
 */
public class BoyerMoore implements BuscadorPatron {

    private static final int TAMANO_ALFABETO = 256;

    @Override
    public String nombre() {
        return "Boyer-Moore";
    }

    /** ultima[c] = última aparición de c en el patrón. Caracteres fuera de 0-255 no se registran. */
    public int[] construirTabla(char[] p) {
        int[] ultima = new int[TAMANO_ALFABETO];
        for (int c = 0; c < TAMANO_ALFABETO; c++) {
            ultima[c] = -1;
        }
        for (int k = 0; k < p.length; k++) {
            if (p[k] < TAMANO_ALFABETO) {
                ultima[p[k]] = k;
            }
        }
        return ultima;
    }

    private int ultima(int[] tabla, char c) {
        return c < TAMANO_ALFABETO ? tabla[c] : -1; // fuera de 0-255: "no está en el patrón"
    }

    @Override
    public ResultadoBusqueda buscar(char[] t, char[] p) {
        List<Integer> posiciones = new ArrayList<>();
        long comparaciones = 0;
        int n = t.length;
        int m = p.length;

        long inicioPre = System.nanoTime();
        int[] tabla = construirTabla(p);
        long preNs = System.nanoTime() - inicioPre;

        long inicioBusqueda = System.nanoTime();
        int s = 0; // posición del texto donde está alineado el inicio del patrón
        while (s <= n - m) {
            int j = m - 1;
            while (j >= 0) {
                comparaciones++;
                if (p[j] != t[s + j]) {
                    break;
                }
                j--;
            }
            if (j < 0) {
                posiciones.add(s);
                // Coincidencia: alinea el siguiente carácter del texto con su última aparición en el patrón
                s += (s + m < n) ? m - ultima(tabla, t[s + m]) : 1;
            } else {
                s += Math.max(1, j - ultima(tabla, t[s + j]));
            }
        }
        long busquedaNs = System.nanoTime() - inicioBusqueda;

        return new ResultadoBusqueda(posiciones, comparaciones, 0, preNs, busquedaNs);
    }

    @Override
    public void imprimirTraza(String texto, String patron) {
        char[] t = texto.toCharArray();
        char[] p = patron.toCharArray();
        int n = t.length;
        int m = p.length;
        int[] tabla = construirTabla(p);

        System.out.println("--- Boyer-Moore: tabla del carácter malo (solo los que están en el patrón) ---");
        for (int c = 0; c < TAMANO_ALFABETO; c++) {
            if (tabla[c] >= 0) {
                System.out.println("  ultima['" + (char) c + "'] = " + tabla[c]);
            }
        }
        System.out.println("  (cualquier otro carácter: -1)\n");

        int s = 0;
        int alineacion = 1;
        while (s <= n - m) {
            System.out.println("Alineación " + alineacion++ + " (s = " + s + ")");
            System.out.println("  texto : " + texto);
            System.out.println("  patrón: " + " ".repeat(s) + patron);

            int j = m - 1;
            while (j >= 0 && p[j] == t[s + j]) {
                j--;
            }
            if (j < 0) {
                int desplazamiento = (s + m < n) ? m - ultima(tabla, t[s + m]) : 1;
                System.out.println("  COINCIDENCIA en la posición " + s + " -> desplazamiento " + desplazamiento);
                s += desplazamiento;
            } else {
                int u = ultima(tabla, t[s + j]);
                int desplazamiento = Math.max(1, j - u);
                System.out.println("  Falla en j = " + j + ": texto '" + t[s + j] + "' vs patrón '" + p[j]
                        + "'; ultima['" + t[s + j] + "'] = " + u + " -> desplazamiento max(1, "
                        + j + " - " + u + ") = " + desplazamiento);
                s += desplazamiento;
            }
        }
        ResultadoBusqueda r = buscar(t, p);
        System.out.println("\nCoincidencias en " + r.posiciones + " con " + r.comparaciones + " comparaciones.");
    }
}
