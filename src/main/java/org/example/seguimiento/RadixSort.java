package org.example.seguimiento;

/**
 * Radix Sort LSD (dígito menos significativo primero), base 10,
 * descendente y estable.
 * Complejidad: O(d (n + b)), con d = dígitos del máximo y b = 10.
 */
public class RadixSort implements Ordenamiento {

    private static final int BASE = 10;

    @Override
    public String nombre() {
        return "Radix Sort";
    }

    @Override
    public ResultadoOrden ordenar(String[] palabras, int[] frecuencias) {

        int n = frecuencias.length;

        // Copia de trabajo para no modificar la entrada: O(n)
        String[] palabrasRadix = palabras.clone();
        int[] frecuenciasRadix = frecuencias.clone();

        String[] palabrasAuxiliares = new String[n];
        int[] frecuenciasAuxiliares = new int[n];

        // Buscar el máximo para saber cuántos dígitos hay: O(n)
        int mayor = frecuenciasRadix[0];
        for (int i = 1; i < n; i++) {
            if (frecuenciasRadix[i] > mayor) {
                mayor = frecuenciasRadix[i];
            }
        }

        // Una pasada de Counting Sort estable por cada dígito: d veces O(n + b)
        for (int exp = 1; mayor / exp > 0; exp = exp * BASE) {

            int[] conteoDigitos = new int[BASE];

            // 1. Contar
            for (int i = 0; i < n; i++) {
                int digito = (frecuenciasRadix[i] / exp) % BASE;
                conteoDigitos[digito]++;
            }

            // 2. Acumular de mayor a menor (orden descendente)
            for (int i = BASE - 2; i >= 0; i--) {
                conteoDigitos[i] = conteoDigitos[i] + conteoDigitos[i + 1];
            }

            // 3. Colocar de atrás hacia adelante (estable)
            for (int i = n - 1; i >= 0; i--) {
                int digito = (frecuenciasRadix[i] / exp) % BASE;
                int posicion = conteoDigitos[digito] - 1;

                palabrasAuxiliares[posicion] = palabrasRadix[i];
                frecuenciasAuxiliares[posicion] = frecuenciasRadix[i];

                conteoDigitos[digito]--;
            }

            // 4. Copiar al arreglo principal
            for (int i = 0; i < n; i++) {
                palabrasRadix[i] = palabrasAuxiliares[i];
                frecuenciasRadix[i] = frecuenciasAuxiliares[i];
            }
        }

        return new ResultadoOrden(palabrasRadix, frecuenciasRadix);
    }
}
