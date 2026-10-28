package org.example.seguimiento;

/**
 * Interfaz común para los algoritmos de ordenamiento.
 * Ningún algoritmo modifica los arreglos de entrada.
 */
public interface Ordenamiento {

    String nombre();

    ResultadoOrden ordenar(String[] palabras, int[] frecuencias);
}
