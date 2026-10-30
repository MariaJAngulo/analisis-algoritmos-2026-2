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

/** Provisional (Fase 4): R1, R2 y R3 por consola. El menú completo llega en la Fase 5. */
public class Main {
    public static void main(String[] args) throws Exception {
        Corpus corpus = Corpus.desde("datos/articulos.json");
        Scanner teclado = new Scanner(System.in);

        while (true) {
            System.out.println("\n1. Similitud entre artículos (R1)");
            System.out.println("2. Clustering jerárquico y dendrogramas (R2)");
            System.out.println("3. Búsqueda de patrones (R3)");
            System.out.println("0. Salir");
            System.out.print("Opción: ");
            String opcion = teclado.nextLine().trim();

            if (opcion.equals("0")) {
                break;
            } else if (opcion.equals("1")) {
                similitud(corpus, teclado);
            } else if (opcion.equals("2")) {
                clustering(corpus);
            } else if (opcion.equals("3")) {
                busqueda(corpus, teclado);
            } else {
                System.out.println("Opción no válida.");
            }
        }
    }

    private static void similitud(Corpus corpus, Scanner teclado) throws Exception {
        ComparadorSimilitud comparador = new ComparadorSimilitud(corpus.articulos(),
                "datos/glove_corpus.txt", "datos/embeddings_minilm.json");
        System.out.print("Ids separados por coma (ej. 1, 4, 15): ");
        try {
            List<Articulo> seleccion = ComparadorSimilitud.seleccionar(corpus, teclado.nextLine());
            ResultadoSimilitud resultado = comparador.comparar(seleccion);
            comparador.imprimir(resultado);
            comparador.guardarCsv(resultado, "resultados/similitud.csv");
            System.out.println("\nGuardado en resultados/similitud.csv");
        } catch (IllegalArgumentException error) {
            System.out.println("Error: " + error.getMessage());
        }
    }

    private static void clustering(Corpus corpus) throws Exception {
        AnalisisClustering analisis = new AnalisisClustering(corpus.articulos());
        List<ResultadoClustering> resultados = analisis.ejecutar();
        analisis.imprimir(resultados);
        analisis.guardarDendrogramas(resultados, "resultados");
        System.out.println("\nDendrogramas guardados en resultados/dendrograma_simple.svg, "
                + "_completo.svg y _promedio.svg");
    }

    private static void busqueda(Corpus corpus, Scanner teclado) throws Exception {
        BusquedaPatrones busqueda = new BusquedaPatrones(corpus.articulos());
        System.out.print("Patrón a buscar (ej. learning, artificial intelligence): ");
        try {
            ResultadoBusquedaPatron resultado = busqueda.buscar(teclado.nextLine());
            busqueda.imprimir(resultado);
            busqueda.guardarCsv(resultado, "resultados/busqueda.csv");
            System.out.println("\nGuardado en resultados/busqueda.csv");
        } catch (IllegalArgumentException error) {
            System.out.println("Error: " + error.getMessage());
        }
    }
}
