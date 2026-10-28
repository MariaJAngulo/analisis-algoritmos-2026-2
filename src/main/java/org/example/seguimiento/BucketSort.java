package org.example.seguimiento;

/**
 * Bucket Sort descendente y estable.
 *
 * Reparte cada frecuencia f en la cubeta f * m / (max + 1), ordena
 * cada cubeta con inserción y concatena las cubetas de la mayor a
 * la menor.
 *
 * Con m = 10 cubetas (versión original) casi todas las palabras caen
 * en la cubeta 0, porque la mayoría aparece 1 o 2 veces (ley de Zipf),
 * y la inserción en esa cubeta cuesta O(n^2).
 *
 * Con m = max + 1 cubetas (una por cada valor de frecuencia) cada
 * cubeta solo contiene valores iguales, la inserción no mueve nada y
 * el total es O(n + k).
 */
public class BucketSort implements Ordenamiento {

    /** Valor especial: usar una cubeta por cada valor de frecuencia. */
    public static final int UNA_CUBETA_POR_FRECUENCIA = -1;

    private final int cubetasSolicitadas;

    public BucketSort(int cantidadCubetas) {
        this.cubetasSolicitadas = cantidadCubetas;
    }

    @Override
    public String nombre() {
        if (cubetasSolicitadas == UNA_CUBETA_POR_FRECUENCIA) {
            return "Bucket Sort (k cubetas)";
        }
        return "Bucket Sort (" + cubetasSolicitadas + " cubetas)";
    }

    private static int buscarMayor(int[] frecuencias) {
        int mayor = frecuencias[0];
        for (int i = 1; i < frecuencias.length; i++) {
            if (frecuencias[i] > mayor) {
                mayor = frecuencias[i];
            }
        }
        return mayor;
    }

    private int cantidadCubetas(int mayor) {
        if (cubetasSolicitadas == UNA_CUBETA_POR_FRECUENCIA) {
            return mayor + 1;
        }
        return cubetasSolicitadas;
    }

    private static int cubetaDe(int frecuencia, int cantidadCubetas, int mayor) {
        int cubeta = (int) ((long) frecuencia * cantidadCubetas / (mayor + 1));
        if (cubeta >= cantidadCubetas) {
            cubeta = cantidadCubetas - 1;
        }
        return cubeta;
    }

    /** Cuántos elementos caen en cada cubeta (para el análisis). */
    public int[] tamanosCubetas(int[] frecuencias) {
        int mayor = buscarMayor(frecuencias);
        int m = cantidadCubetas(mayor);
        int[] tamanos = new int[m];
        for (int frecuencia : frecuencias) {
            tamanos[cubetaDe(frecuencia, m, mayor)]++;
        }
        return tamanos;
    }

    @Override
    public ResultadoOrden ordenar(String[] palabras, int[] frecuencias) {

        int n = frecuencias.length;
        int mayor = buscarMayor(frecuencias);
        int m = cantidadCubetas(mayor);

        // 1. Contar cuántos elementos van a cada cubeta: O(n + m)
        int[] tamanos = new int[m];
        for (int i = 0; i < n; i++) {
            tamanos[cubetaDe(frecuencias[i], m, mayor)]++;
        }

        // 2. Calcular dónde empieza cada cubeta en un solo arreglo.
        //    Las cubetas se ubican de la mayor a la menor: O(m)
        int[] inicio = new int[m];
        int acumulado = 0;
        for (int c = m - 1; c >= 0; c--) {
            inicio[c] = acumulado;
            acumulado += tamanos[c];
        }

        // 3. Repartir los elementos en sus cubetas: O(n)
        String[] palabrasCubetas = new String[n];
        int[] frecuenciasCubetas = new int[n];
        int[] llenado = new int[m];

        for (int i = 0; i < n; i++) {
            int c = cubetaDe(frecuencias[i], m, mayor);
            int posicion = inicio[c] + llenado[c];
            palabrasCubetas[posicion] = palabras[i];
            frecuenciasCubetas[posicion] = frecuencias[i];
            llenado[c]++;
        }

        // 4. Ordenar cada cubeta con inserción (descendente, estable).
        //    Costo: suma de O(n_c^2) en el peor caso.
        for (int c = 0; c < m; c++) {
            int desde = inicio[c];
            int hasta = inicio[c] + tamanos[c];

            for (int i = desde + 1; i < hasta; i++) {
                int frecuenciaActual = frecuenciasCubetas[i];
                String palabraActual = palabrasCubetas[i];

                int j = i - 1;
                while (j >= desde && frecuenciasCubetas[j] < frecuenciaActual) {
                    frecuenciasCubetas[j + 1] = frecuenciasCubetas[j];
                    palabrasCubetas[j + 1] = palabrasCubetas[j];
                    j--;
                }
                frecuenciasCubetas[j + 1] = frecuenciaActual;
                palabrasCubetas[j + 1] = palabraActual;
            }
        }

        // Como las cubetas ya quedaron una tras otra (de mayor a menor),
        // el arreglo completo es el resultado concatenado.
        return new ResultadoOrden(palabrasCubetas, frecuenciasCubetas);
    }
}
