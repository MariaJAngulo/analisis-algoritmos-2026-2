package org.example.seguimiento;

/**
 * Counting Sort descendente y estable.
 * Complejidad: O(n + k), con k = frecuencia máxima + 1.
 */
public class CountingSort implements Ordenamiento {

    @Override
    public String nombre() {
        return "Counting Sort";
    }

    @Override
    public ResultadoOrden ordenar(String[] palabras, int[] frecuencias) {

        int n = frecuencias.length;

        // 1. Buscar el máximo: O(n)
        int mayor = frecuencias[0];
        for (int i = 1; i < n; i++) {
            if (frecuencias[i] > mayor) {
                mayor = frecuencias[i];
            }
        }

        // 2. Contar cuántas veces aparece cada frecuencia: O(k) + O(n)
        int[] conteo = new int[mayor + 1];
        for (int i = 0; i < n; i++) {
            conteo[frecuencias[i]]++;
        }

        // 3. Acumular de mayor a menor para obtener orden descendente: O(k)
        for (int i = mayor - 1; i >= 0; i--) {
            conteo[i] = conteo[i] + conteo[i + 1];
        }

        // 4. Colocar cada elemento en su posición final: O(n)
        //    Se recorre de atrás hacia adelante para que sea estable.
        String[] palabrasOrdenadas = new String[n];
        int[] frecuenciasOrdenadas = new int[n];

        for (int i = n - 1; i >= 0; i--) {
            int frecuencia = frecuencias[i];
            int posicion = conteo[frecuencia] - 1;

            palabrasOrdenadas[posicion] = palabras[i];
            frecuenciasOrdenadas[posicion] = frecuencia;

            conteo[frecuencia]--;
        }

        return new ResultadoOrden(palabrasOrdenadas, frecuenciasOrdenadas);
    }
}
