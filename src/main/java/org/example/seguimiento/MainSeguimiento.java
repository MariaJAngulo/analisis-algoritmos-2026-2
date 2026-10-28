package org.example.seguimiento;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.example.texto.Preprocesador;
import org.example.util.Medidor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * Requerimiento 3: ranking de términos con algoritmos de ordenamiento
 * no basados en comparaciones (Counting Sort, Radix Sort y Bucket Sort).
 *
 * Los resultados se imprimen en consola y se guardan en la carpeta
 * "resultados" en archivos CSV para construir tablas y gráficas.
 */
public class MainSeguimiento {


    private static final int TOP = 15;

    // Medición sobre el corpus
    private static final int CALENTAMIENTO_CORPUS = 200;
    private static final int REPETICIONES_CORPUS = 1000;

    // Prueba de escalabilidad
    private static final int[] TAMANOS = {1_000, 10_000, 100_000, 1_000_000};
    private static final int LIMITE_BUCKET_10 = 100_000; // más allá tarda demasiado (O(n^2))
    private static final long SEMILLA = 42;

    public static void main(String[] args) throws Exception {
        System.out.println("Working dir: " + new File("").getAbsolutePath());
        Locale.setDefault(Locale.US);
        new File("resultados").mkdirs();

        // 1. Leer el archivo JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode raiz = mapper.readTree(new File("datos/articulos.json"));
        JsonNode articulos = raiz.get("papers");

        System.out.println("--- COMPROBACIÓN DE RESÚMENES ---");
        for (int i = 0; i < articulos.size(); i++) {
            String resumen = articulos.get(i).get("Abstract").asText();
            System.out.println("Artículo " + (i + 1)
                    + ": caracteres del resumen = " + resumen.length());
        }

        // 2. Preprocesamiento y conteo de frecuencias
        Preprocesador preprocesador = new Preprocesador();
        ResultadoOrden entrada = preprocesador.contarFrecuencias(articulos);
        String[] palabras = entrada.palabras;
        int[] frecuencias = entrada.frecuencias;

        int mayor = 0;
        for (int f : frecuencias) {
            mayor = Math.max(mayor, f);
        }

        System.out.println("\n--- DATOS DEL CORPUS ---");
        System.out.println("Total de artículos: " + articulos.size());
        System.out.println("Palabras procesadas: " + preprocesador.getTotalPalabrasProcesadas());
        System.out.println("Palabras conservadas (sin stopwords): " + preprocesador.getTotalPalabrasConservadas());
        System.out.println("Stopwords en la lista: " + preprocesador.cantidadPalabrasVacias());
        System.out.println("Palabras diferentes (n): " + palabras.length);
        System.out.println("Frecuencia máxima: " + mayor + "  (k = " + (mayor + 1) + ")");

        // 3. Algoritmos
        Ordenamiento[] algoritmos = {
                new CountingSort(),
                new RadixSort(),
                new BucketSort(10),
                new BucketSort(BucketSort.UNA_CUBETA_POR_FRECUENCIA)
        };

        ResultadoOrden[] resultados = new ResultadoOrden[algoritmos.length];
        for (int a = 0; a < algoritmos.length; a++) {
            resultados[a] = algoritmos[a].ordenar(palabras, frecuencias);
        }

        // 4. Validación: todos deben dar exactamente el mismo orden
        System.out.println("\n--- VALIDACIÓN ---");
        for (int a = 1; a < algoritmos.length; a++) {
            System.out.println(algoritmos[a].nombre() + " igual a Counting Sort (lista completa): "
                    + (resultados[a].esIgualA(resultados[0]) ? "SÍ" : "NO"));
        }

        // 5. Top 15
        imprimirTop(algoritmos, resultados);
        guardarTop(algoritmos, resultados, "resultados/top15.csv");

        // 6. Distribución de las cubetas
        imprimirCubetas(new BucketSort(10), frecuencias, "resultados/cubetas_10.csv");

        // 7. Tiempos sobre el corpus
        System.out.println("\n--- TIEMPOS SOBRE EL CORPUS (ms) ---");
        System.out.println("Calentamiento: " + CALENTAMIENTO_CORPUS
                + " ejecuciones, medición: " + REPETICIONES_CORPUS + " repeticiones");
        System.out.printf("%-26s %10s %10s %10s %10s%n",
                "Algoritmo", "Promedio", "Mínimo", "Máximo", "Desv.");

        try (PrintWriter csv = new PrintWriter("resultados/tiempos_corpus.csv", "UTF-8")) {
            csv.println("algoritmo,promedio_ms,minimo_ms,maximo_ms,desviacion_ms,repeticiones");
            for (Ordenamiento algoritmo : algoritmos) {
                Medidor.Estadistica e = Medidor.medir(
                        () -> algoritmo.ordenar(palabras, frecuencias),
                        CALENTAMIENTO_CORPUS, REPETICIONES_CORPUS);
                System.out.printf("%-26s %10.4f %10.4f %10.4f %10.4f%n",
                        algoritmo.nombre(), e.promedioMs, e.minimoMs, e.maximoMs, e.desviacionMs);
                csv.printf("%s,%.6f,%.6f,%.6f,%.6f,%d%n", algoritmo.nombre(),
                        e.promedioMs, e.minimoMs, e.maximoMs, e.desviacionMs, e.repeticiones);
            }
        }

        // 8. Escalabilidad
        System.out.println("\n--- ESCALABILIDAD: TIEMPO PROMEDIO (ms) vs n ---");
        System.out.printf("%-10s", "n");
        for (Ordenamiento algoritmo : algoritmos) {
            System.out.printf(" %26s", algoritmo.nombre());
        }
        System.out.println();

        try (PrintWriter csv = new PrintWriter("resultados/escalabilidad.csv", "UTF-8")) {
            csv.println("n,algoritmo,promedio_ms,desviacion_ms");
            for (int n : TAMANOS) {
                ResultadoOrden datos = Escalabilidad.generar(n, frecuencias, SEMILLA);
                int calentamiento = n >= 1_000_000 ? 3 : 10;
                int repeticiones = n >= 1_000_000 ? 10 : 30;

                System.out.printf("%-10d", n);
                for (Ordenamiento algoritmo : algoritmos) {
                    boolean esBucket10 = algoritmo.nombre().equals("Bucket Sort (10 cubetas)");
                    if (esBucket10 && n > LIMITE_BUCKET_10) {
                        System.out.printf(" %26s", "(omitido)");
                        continue;
                    }
                    int cal = esBucket10 && n >= 100_000 ? 1 : calentamiento;
                    int rep = esBucket10 && n >= 100_000 ? 3 : repeticiones;
                    Medidor.Estadistica e = Medidor.medir(
                            () -> algoritmo.ordenar(datos.palabras, datos.frecuencias), cal, rep);
                    System.out.printf(" %26.4f", e.promedioMs);
                    csv.printf("%d,%s,%.6f,%.6f%n", n, algoritmo.nombre(),
                            e.promedioMs, e.desviacionMs);
                }
                System.out.println();
            }
        }

        System.out.println("\nResultados guardados en la carpeta 'resultados'.");
    }

    private static void imprimirTop(Ordenamiento[] algoritmos, ResultadoOrden[] resultados) {
        System.out.println("\n--- TOP " + TOP + " TÉRMINOS MÁS FRECUENTES ---");
        System.out.printf("%-4s", "#");
        for (Ordenamiento algoritmo : algoritmos) {
            System.out.printf(" %-26s", algoritmo.nombre());
        }
        System.out.println();

        int limite = Math.min(TOP, resultados[0].palabras.length);
        for (int i = 0; i < limite; i++) {
            System.out.printf("%-4d", i + 1);
            for (ResultadoOrden r : resultados) {
                System.out.printf(" %-26s", r.palabras[i] + " (" + r.frecuencias[i] + ")");
            }
            System.out.println();
        }

        // Avisar si hay palabras empatadas con la última posición que quedan por fuera
        ResultadoOrden r = resultados[0];
        if (r.palabras.length > TOP && r.frecuencias[TOP] == r.frecuencias[TOP - 1]) {
            StringBuilder fuera = new StringBuilder();
            for (int i = TOP; i < r.palabras.length && r.frecuencias[i] == r.frecuencias[TOP - 1]; i++) {
                fuera.append(r.palabras[i]).append(" ");
            }
            System.out.println("Empatadas con la posición " + TOP + " (frecuencia "
                    + r.frecuencias[TOP - 1] + ") que quedan fuera por orden alfabético: "
                    + fuera.toString().trim());
        }
    }

    private static void guardarTop(Ordenamiento[] algoritmos, ResultadoOrden[] resultados,
                                   String archivo) throws IOException {
        try (PrintWriter csv = new PrintWriter(archivo, "UTF-8")) {
            StringBuilder encabezado = new StringBuilder("posicion");
            for (Ordenamiento algoritmo : algoritmos) {
                encabezado.append(",").append(algoritmo.nombre()).append(" termino")
                        .append(",").append(algoritmo.nombre()).append(" frecuencia");
            }
            csv.println(encabezado);
            for (int i = 0; i < TOP && i < resultados[0].palabras.length; i++) {
                StringBuilder fila = new StringBuilder(String.valueOf(i + 1));
                for (ResultadoOrden r : resultados) {
                    fila.append(",").append(r.palabras[i]).append(",").append(r.frecuencias[i]);
                }
                csv.println(fila);
            }
        }
    }

    private static void imprimirCubetas(BucketSort bucket, int[] frecuencias,
                                        String archivo) throws IOException {
        int[] tamanos = bucket.tamanosCubetas(frecuencias);
        System.out.println("\n--- DISTRIBUCIÓN EN " + bucket.nombre().toUpperCase() + " ---");
        try (PrintWriter csv = new PrintWriter(archivo, "UTF-8")) {
            csv.println("cubeta,palabras");
            for (int c = 0; c < tamanos.length; c++) {
                System.out.printf("Cubeta %d: %d palabras (%.1f %%)%n",
                        c, tamanos[c], 100.0 * tamanos[c] / frecuencias.length);
                csv.println(c + "," + tamanos[c]);
            }
        }
    }
}
