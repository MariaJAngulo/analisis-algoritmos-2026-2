package org.example;

import org.example.busqueda.BusquedaPatrones;
import org.example.busqueda.ResultadoBusquedaPatron;
import org.example.clustering.AnalisisClustering;
import org.example.clustering.ResultadoClustering;
import org.example.modelo.Articulo;
import org.example.modelo.Corpus;
import org.example.similitud.ComparadorSimilitud;
import org.example.similitud.ResultadoSimilitud;

import java.util.List;
import java.util.Scanner;

/**
 * Menú de consola del proyecto. Cada opción llama a las mismas clases que usa la página web:
 * 1. Similitud (R1)   2. Clustering jerárquico (R2)   3. Búsqueda de patrones (R3)
 * Los resultados también se guardan en la carpeta "resultados".
 * Debe ejecutarse desde la raíz del proyecto (donde está la carpeta "datos").
 */
public class Main {

    private static final String RUTA_CORPUS = "datos/articulos.json";
    private static final String RUTA_GLOVE = "datos/glove_corpus.txt";
    private static final String RUTA_MINILM = "datos/embeddings_minilm.json";
    private static final String CARPETA_RESULTADOS = "resultados";

    public static void main(String[] args) throws Exception {
        Corpus corpus = Corpus.desde(RUTA_CORPUS);
        ComparadorSimilitud comparador = new ComparadorSimilitud(corpus.articulos(), RUTA_GLOVE, RUTA_MINILM);
        AnalisisClustering clustering = new AnalisisClustering(corpus.articulos());
        BusquedaPatrones busqueda = new BusquedaPatrones(corpus.articulos());
        Scanner teclado = new Scanner(System.in);

        System.out.println("Análisis de algoritmos: " + corpus.articulos().size() + " artículos cargados.");
        while (true) {
            System.out.println("\n=== MENÚ ===");
            System.out.println("1. Similitud entre artículos (R1)");
            System.out.println("2. Clustering jerárquico y dendrogramas (R2)");
            System.out.println("3. Búsqueda de patrones (R3)");
            System.out.println("4. Listar artículos (id, título, autores)");
            System.out.println("0. Salir");
            System.out.print("Opción: ");
            String opcion = teclado.nextLine().trim();

            if (opcion.equals("0")) {
                System.out.println("Hasta luego.");
                break;
            } else if (opcion.equals("1")) {
                similitud(corpus, comparador, teclado);
            } else if (opcion.equals("2")) {
                clustering(clustering);
            } else if (opcion.equals("3")) {
                busqueda(busqueda, teclado);
            } else if (opcion.equals("4")) {
                listar(corpus);
            } else {
                System.out.println("Opción no válida. Escribe 0, 1, 2, 3 o 4.");
            }
        }
    }

    private static void similitud(Corpus corpus, ComparadorSimilitud comparador, Scanner teclado) throws Exception {
        System.out.print("Ids separados por coma (ej. 1, 4, 15): ");
        try {
            List<Articulo> seleccion = ComparadorSimilitud.seleccionar(corpus, teclado.nextLine());
            ResultadoSimilitud resultado = comparador.comparar(seleccion);
            comparador.imprimir(resultado);
            comparador.guardarCsv(resultado, CARPETA_RESULTADOS + "/similitud.csv");
            System.out.println("\nGuardado en " + CARPETA_RESULTADOS + "/similitud.csv");
        } catch (IllegalArgumentException error) {
            System.out.println("Error: " + error.getMessage());
        }
    }

    private static void clustering(AnalisisClustering analisis) throws Exception {
        List<ResultadoClustering> resultados = analisis.ejecutar();
        analisis.imprimir(resultados);
        analisis.guardarDendrogramas(resultados, CARPETA_RESULTADOS);
        System.out.println("\nDendrogramas guardados en " + CARPETA_RESULTADOS
                + "/ (dendrograma_simple.svg, dendrograma_completo.svg y dendrograma_promedio.svg)");
    }

    private static void busqueda(BusquedaPatrones busqueda, Scanner teclado) throws Exception {
        System.out.print("Patrón a buscar (ej. learning, artificial intelligence): ");
        try {
            ResultadoBusquedaPatron resultado = busqueda.buscar(teclado.nextLine());
            busqueda.imprimir(resultado);
            busqueda.guardarCsv(resultado, CARPETA_RESULTADOS + "/busqueda.csv");
            System.out.println("\nGuardado en " + CARPETA_RESULTADOS + "/busqueda.csv");
        } catch (IllegalArgumentException error) {
            System.out.println("Error: " + error.getMessage());
        }
    }

    private static void listar(Corpus corpus) {
        System.out.println();
        for (Articulo a : corpus.articulos()) {
            System.out.println("[" + a.id + "] " + a.titulo);
            System.out.println("     Autores: " + a.autoresTexto());
        }
    }
}
