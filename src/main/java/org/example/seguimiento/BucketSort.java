package org.example.seguimiento;

/**
 * Bucket Sort descendente y estable.
 *
 * 1. Se crean m cubetas (arreglos). La frecuencia f va a la cubeta f * m / (max + 1).
 * 2. Cada elemento se guarda en su cubeta.
 * 3. Cada cubeta se ordena con inserción (de mayor a menor frecuencia).
 * 4. Se juntan las cubetas, de la última (frecuencias altas) a la primera.
 *
 * Con m = 10 cubetas casi todas las palabras caen en la cubeta 0, porque la
 * mayoría aparece 1 o 2 veces (ley de Zipf), y la inserción en esa cubeta
 * cuesta O(n^2).
 *
 * Con m = max + 1 cubetas (una por cada valor de frecuencia) cada cubeta solo
 * tiene valores iguales, la inserción no mueve nada y el total es O(n + k).
 */
public class BucketSort implements Ordenamiento {

    private static final int CUBETAS_FIJAS = 10;

    private final boolean unaCubetaPorFrecuencia;

    /** false: 10 cubetas.  true: una cubeta por cada valor de frecuencia (k cubetas). */
    public BucketSort(boolean unaCubetaPorFrecuencia) {
        this.unaCubetaPorFrecuencia = unaCubetaPorFrecuencia;
    }

    @Override
    public String nombre() {
        if (unaCubetaPorFrecuencia) {
            return "Bucket Sort (k cubetas)";
        }
        return "Bucket Sort (10 cubetas)";
    }

    @Override
    public ResultadoOrden ordenar(String[] palabras, int[] frecuencias) {
        int n = frecuencias.length;

        // Buscar la frecuencia mayor: O(n)
        int mayor = frecuencias[0];
        for (int i = 1; i < n; i++) {
            if (frecuencias[i] > mayor) {
                mayor = frecuencias[i];
            }
        }

        // 1. Contar cuántos elementos van a cada cubeta, para crear cada
        //    cubeta (un arreglo) del tamaño justo: O(n + m)
        int m = unaCubetaPorFrecuencia ? mayor + 1 : CUBETAS_FIJAS;
        int[] tamanos = new int[m];
        for (int i = 0; i < n; i++) {
            tamanos[cubetaDe(frecuencias[i], m, mayor)]++;
        }
        int[][] cubetas = new int[m][];
        for (int c = 0; c < m; c++) {
            cubetas[c] = new int[tamanos[c]];
        }

        // 2. Repartir: cada cubeta guarda la POSICIÓN del elemento en la entrada: O(n)
        int[] llenas = new int[m]; // cuántos elementos lleva ya cada cubeta
        for (int i = 0; i < n; i++) {
            int c = cubetaDe(frecuencias[i], m, mayor);
            cubetas[c][llenas[c]] = i;
            llenas[c]++;
        }

        // 3. Ordenar cada cubeta con inserción, de mayor a menor frecuencia.
        //    Solo se mueve un elemento si su frecuencia es MENOR (estable).
        for (int[] cubeta : cubetas) {
            for (int i = 1; i < cubeta.length; i++) {
                int actual = cubeta[i];
                int j = i - 1;
                while (j >= 0 && frecuencias[cubeta[j]] < frecuencias[actual]) {
                    cubeta[j + 1] = cubeta[j];
                    j--;
                }
                cubeta[j + 1] = actual;
            }
        }

        // 4. Juntar las cubetas de la última a la primera: O(n)
        String[] palabrasOrdenadas = new String[n];
        int[] frecuenciasOrdenadas = new int[n];
        int posicion = 0;
        for (int c = m - 1; c >= 0; c--) {
            for (int indice : cubetas[c]) {
                palabrasOrdenadas[posicion] = palabras[indice];
                frecuenciasOrdenadas[posicion] = frecuencias[indice];
                posicion++;
            }
        }
        return new ResultadoOrden(palabrasOrdenadas, frecuenciasOrdenadas);
    }

    /** Cubeta de una frecuencia: f * m / (max + 1), un número entre 0 y m - 1. */
    private static int cubetaDe(int frecuencia, int m, int mayor) {
        return frecuencia * m / (mayor + 1);
    }

    /** Cuántos elementos caen en cada una de las 10 cubetas (para el análisis). */
    public static int[] tamanosCubetas(int[] frecuencias) {
        int mayor = 0;
        for (int f : frecuencias) {
            mayor = Math.max(mayor, f);
        }
        int[] tamanos = new int[CUBETAS_FIJAS];
        for (int f : frecuencias) {
            tamanos[cubetaDe(f, CUBETAS_FIJAS, mayor)]++;
        }
        return tamanos;
    }
}
