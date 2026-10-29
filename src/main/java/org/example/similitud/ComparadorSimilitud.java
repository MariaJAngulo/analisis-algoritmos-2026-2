package org.example.similitud;

import org.example.modelo.Articulo;
import org.example.modelo.Corpus;
import org.example.util.Medidor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Requerimiento 1: compara los abstracts de los artículos elegidos con los 6 algoritmos
 * (4 clásicos + 2 modelos de IA) y presenta matrices, tabla resumen, tiempos y CSV.
 */
public class ComparadorSimilitud {

    private static final int CALENTAMIENTO = 10;
    private static final int REPETICIONES = 30;

    private final Similitud[] algoritmos;

    /** Crea los 6 algoritmos. El coseno TF-IDF necesita el corpus completo para el idf. */
    public ComparadorSimilitud(List<Articulo> corpus, String rutaGlove, String rutaMiniLM)
            throws IOException {
        algoritmos = new Similitud[]{
                new Levenshtein(),
                new SubsecuenciaComunMasLarga(),
                new Jaccard(),
                new CosenoTfIdf(corpus),
                new EmbeddingGlove(rutaGlove),
                new EmbeddingMiniLM(rutaMiniLM)
        };
    }

    /**
     * Convierte "1, 4, 15" en la lista de artículos. Exige al menos 2 ids distintos y que
     * todos existan; si no, lanza IllegalArgumentException con un mensaje para el usuario.
     */
    public static List<Articulo> seleccionar(Corpus corpus, String texto) {
        List<Articulo> elegidos = new ArrayList<>();
        Set<String> vistos = new HashSet<>();
        for (String parte : texto.split(",")) {
            String id = parte.trim();
            if (id.isEmpty()) {
                continue;
            }
            Articulo articulo = corpus.buscarPorId(id);
            if (articulo == null) {
                throw new IllegalArgumentException("No existe el artículo con id '" + id + "'.");
            }
            if (!vistos.add(id)) {
                throw new IllegalArgumentException("El id '" + id + "' está repetido.");
            }
            elegidos.add(articulo);
        }
        if (elegidos.size() < 2) {
            throw new IllegalArgumentException("Elige al menos 2 artículos (ej. 1, 4, 15).");
        }
        return elegidos;
    }

    /**
     * Calcula, por algoritmo, la matriz de similitud entre todos los pares y su tiempo.
     * Cada matriz es simétrica: se calcula la mitad superior y la diagonal vale 1.
     * Los vectores TF-IDF y de los modelos de IA se guardan en memoria; por eso el
     * tiempo medido de esos tres no incluye armar el vector la primera vez.
     */
    public ResultadoSimilitud comparar(List<Articulo> seleccion) {
        int n = seleccion.size();
        double[][][] matrices = new double[algoritmos.length][][];
        Medidor.Estadistica[] tiempos = new Medidor.Estadistica[algoritmos.length];
        List<String> nombresDistancia = new ArrayList<>();
        List<double[][]> distancias = new ArrayList<>();

        for (int a = 0; a < algoritmos.length; a++) {
            Similitud algoritmo = algoritmos[a];
            matrices[a] = matriz(algoritmo, seleccion);
            tiempos[a] = Medidor.medir(() -> matriz(algoritmo, seleccion), CALENTAMIENTO, REPETICIONES);

            if (algoritmo instanceof Embedding) {
                Embedding modelo = (Embedding) algoritmo;
                double[][] d = new double[n][n];
                for (int i = 0; i < n; i++) {
                    for (int j = i + 1; j < n; j++) {
                        d[i][j] = modelo.distanciaEuclidiana(seleccion.get(i), seleccion.get(j));
                        d[j][i] = d[i][j];
                    }
                }
                nombresDistancia.add(algoritmo.nombre());
                distancias.add(d);
            }
        }
        return new ResultadoSimilitud(seleccion, algoritmos, matrices, nombresDistancia,
                distancias, tiempos);
    }

    // O(p · costo del algoritmo), con p = n(n-1)/2 pares
    private double[][] matriz(Similitud algoritmo, List<Articulo> seleccion) {
        int n = seleccion.size();
        double[][] m = new double[n][n];
        for (int i = 0; i < n; i++) {
            m[i][i] = 1.0;
            for (int j = i + 1; j < n; j++) {
                m[i][j] = algoritmo.calcular(seleccion.get(i), seleccion.get(j));
                m[j][i] = m[i][j];
            }
        }
        return m;
    }

    // ---------------------------------------------------------------- consola

    public void imprimir(ResultadoSimilitud r) {
        int n = r.articulos.size();

        System.out.println("\n=== ARTÍCULOS SELECCIONADOS ===");
        for (Articulo a : r.articulos) {
            System.out.println("[" + a.id + "] " + a.titulo);
            System.out.println("     Autores: " + a.autoresTexto());
        }

        for (int k = 0; k < r.algoritmos.length; k++) {
            System.out.println("\n--- Matriz de similitud: " + r.algoritmos[k].nombre() + " ---");
            imprimirMatriz(r.articulos, r.matrices[k], "%8.4f");
        }
        for (int k = 0; k < r.distancias.size(); k++) {
            System.out.println("\n--- Distancia euclidiana: " + r.nombresDistancia.get(k) + " ---");
            imprimirMatriz(r.articulos, r.distancias.get(k), "%8.3f");
        }

        System.out.println("\n=== RESUMEN POR PAR (similitud 0 a 1) ===");
        System.out.printf("%-10s", "Par");
        for (Similitud s : r.algoritmos) {
            System.out.printf(" %17s", s.nombre());
        }
        System.out.println();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                System.out.printf("%-10s", r.articulos.get(i).id + " - " + r.articulos.get(j).id);
                for (int k = 0; k < r.algoritmos.length; k++) {
                    System.out.printf(" %17.4f", r.matrices[k][i][j]);
                }
                System.out.println();
            }
        }

        System.out.println("\n=== TIEMPO DE CALCULAR TODOS LOS PARES (ms) ===");
        System.out.printf("%-18s %10s %10s %10s %10s%n", "Algoritmo", "Promedio", "Mínimo", "Máximo", "Desv.");
        for (int k = 0; k < r.algoritmos.length; k++) {
            Medidor.Estadistica e = r.tiempos[k];
            System.out.printf("%-18s %10.4f %10.4f %10.4f %10.4f%n", r.algoritmos[k].nombre(),
                    e.promedioMs, e.minimoMs, e.maximoMs, e.desviacionMs);
        }
    }

    private void imprimirMatriz(List<Articulo> articulos, double[][] m, String formato) {
        System.out.printf("%-8s", "");
        for (Articulo a : articulos) {
            System.out.printf("%8s", "[" + a.id + "]");
        }
        System.out.println();
        for (int i = 0; i < articulos.size(); i++) {
            System.out.printf("%-8s", "[" + articulos.get(i).id + "]");
            for (int j = 0; j < articulos.size(); j++) {
                System.out.print(String.format(formato, m[i][j]));
            }
            System.out.println();
        }
    }

    // -------------------------------------------------------------------- CSV

    /** Guarda un renglón por par (con título de cada artículo) y, al final, los tiempos. */
    public void guardarCsv(ResultadoSimilitud r, String ruta) throws IOException {
        java.io.File carpeta = new java.io.File(ruta).getAbsoluteFile().getParentFile();
        if (carpeta != null) {
            carpeta.mkdirs();
        }
        try (PrintWriter csv = new PrintWriter(ruta, "UTF-8")) {
            StringBuilder encabezado = new StringBuilder("id_a,titulo_a,autores_a,id_b,titulo_b,autores_b");
            for (Similitud s : r.algoritmos) {
                encabezado.append(',').append(campo(s.nombre()));
            }
            for (String nombre : r.nombresDistancia) {
                encabezado.append(',').append(campo("Euclidiana " + nombre));
            }
            csv.println(encabezado);

            int n = r.articulos.size();
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    Articulo a = r.articulos.get(i);
                    Articulo b = r.articulos.get(j);
                    StringBuilder fila = new StringBuilder();
                    fila.append(campo(a.id)).append(',').append(campo(a.titulo)).append(',')
                            .append(campo(a.autoresTexto())).append(',').append(campo(b.id)).append(',')
                            .append(campo(b.titulo)).append(',').append(campo(b.autoresTexto()));
                    for (int k = 0; k < r.algoritmos.length; k++) {
                        fila.append(',').append(String.format(Locale.US, "%.6f", r.matrices[k][i][j]));
                    }
                    for (double[][] d : r.distancias) {
                        fila.append(',').append(String.format(Locale.US, "%.6f", d[i][j]));
                    }
                    csv.println(fila);
                }
            }

            csv.println();
            csv.println("algoritmo,promedio_ms,minimo_ms,maximo_ms,desviacion_ms,repeticiones");
            for (int k = 0; k < r.algoritmos.length; k++) {
                Medidor.Estadistica e = r.tiempos[k];
                csv.println(String.format(Locale.US, "%s,%.6f,%.6f,%.6f,%.6f,%d",
                        campo(r.algoritmos[k].nombre()), e.promedioMs, e.minimoMs, e.maximoMs,
                        e.desviacionMs, e.repeticiones));
            }
        }
    }

    /** Entre comillas: los títulos y la lista de autores traen comas. */
    private String campo(String texto) {
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }
}