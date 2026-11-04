package org.example.util;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Mide el tiempo de cualquier algoritmo de forma repetible:
 * primero lo ejecuta varias veces sin medir (calentamiento de la JVM
 * para que el compilador JIT optimice el código) y luego mide varias
 * repeticiones y calcula estadísticas en milisegundos.
 */
public class Medidor {

    public static class Estadistica {
        public final double promedioMs;
        public final double minimoMs;
        public final double maximoMs;
        public final double desviacionMs;
        public final double medianaMs;
        public final int repeticiones;

        Estadistica(double promedioMs, double minimoMs, double maximoMs,
                    double desviacionMs, double medianaMs, int repeticiones) {
            this.promedioMs = promedioMs;
            this.minimoMs = minimoMs;
            this.maximoMs = maximoMs;
            this.desviacionMs = desviacionMs;
            this.medianaMs = medianaMs;
            this.repeticiones = repeticiones;
        }
    }

    // Aquí se guarda el resultado de cada ejecución. Si nadie usara el
    // resultado, Java podría saltarse el cálculo y el tiempo saldría falso.
    public static Object ultimoResultado;

    /** Ejecuta la tarea "calentamiento" veces sin medir y luego "repeticiones" veces midiendo. */
    public static Estadistica medir(Supplier<?> tarea, int calentamiento, int repeticiones) {

        for (int i = 0; i < calentamiento; i++) {
            ultimoResultado = tarea.get();
        }

        // Limpiar la memoria antes de medir: así la basura que dejaron el
        // calentamiento o la medición anterior no provoca una pausa del
        // recolector de basura (GC) dentro de las repeticiones medidas.
        System.gc();

        double[] tiempos = new double[repeticiones];

        for (int i = 0; i < repeticiones; i++) {
            long inicio = System.nanoTime();
            ultimoResultado = tarea.get();
            long fin = System.nanoTime();

            tiempos[i] = (fin - inicio) / 1_000_000.0;
        }

        double suma = 0;
        double minimo = Double.MAX_VALUE;
        double maximo = 0;
        for (double t : tiempos) {
            suma += t;
            minimo = Math.min(minimo, t);
            maximo = Math.max(maximo, t);
        }
        double promedio = suma / repeticiones;

        double sumaCuadrados = 0;
        for (double t : tiempos) {
            sumaCuadrados += (t - promedio) * (t - promedio);
        }
        double desviacion = Math.sqrt(sumaCuadrados / repeticiones);

        // Mediana: el tiempo del centro. A diferencia del promedio, no se deja
        // arrastrar por unas pocas repeticiones lentas (por ejemplo, las que
        // coinciden con una pausa del GC).
        double[] ordenados = tiempos.clone();
        Arrays.sort(ordenados);
        double mediana = ordenados[repeticiones / 2];

        return new Estadistica(promedio, minimo, maximo, desviacion, mediana, repeticiones);
    }
}
