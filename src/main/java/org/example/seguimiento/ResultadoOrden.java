package org.example.seguimiento;

/**
 * Resultado de un ordenamiento: las palabras y sus frecuencias
 * en orden descendente de frecuencia.
 */
public class ResultadoOrden {

    public final String[] palabras;
    public final int[] frecuencias;

    public ResultadoOrden(String[] palabras, int[] frecuencias) {
        this.palabras = palabras;
        this.frecuencias = frecuencias;
    }

    /** Devuelve true si los dos resultados tienen exactamente el mismo orden. */
    public boolean esIgualA(ResultadoOrden otro) {
        if (palabras.length != otro.palabras.length) {
            return false;
        }
        for (int i = 0; i < palabras.length; i++) {
            if (!palabras[i].equals(otro.palabras[i])
                    || frecuencias[i] != otro.frecuencias[i]) {
                return false;
            }
        }
        return true;
    }
}
