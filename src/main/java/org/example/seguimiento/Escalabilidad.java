package org.example.seguimiento;

import java.util.Random;

/**
 * Genera listas de frecuencias más grandes que el corpus para ver cómo
 * crece el tiempo de cada algoritmo con n.
 *
 * Cada frecuencia se toma al azar de las frecuencias reales del corpus,
 * así la lista sintética conserva la misma distribución (muchas palabras
 * con frecuencia 1 o 2 y pocas muy frecuentes).
 */
public class Escalabilidad {

    public static ResultadoOrden generar(int n, int[] frecuenciasCorpus, long semilla) {
        Random aleatorio = new Random(semilla);
        String[] palabras = new String[n];
        int[] frecuencias = new int[n];
        for (int i = 0; i < n; i++) {
            palabras[i] = "t" + i;
            frecuencias[i] = frecuenciasCorpus[aleatorio.nextInt(frecuenciasCorpus.length)];
        }
        return new ResultadoOrden(palabras, frecuencias);
    }
}
