package org.example;

import org.example.modelo.Articulo;
import org.example.modelo.Corpus;
import org.example.similitud.ComparadorSimilitud;
import org.example.similitud.ResultadoSimilitud;

import java.util.List;
import java.util.Scanner;

/** Provisional (Fase 2D): prueba el R1 por consola. El menú completo llega en la Fase 5. */
public class Main {
    public static void main(String[] args) throws Exception {
        Corpus corpus = Corpus.desde("datos/articulos.json");
        ComparadorSimilitud comparador = new ComparadorSimilitud(corpus.articulos(),
                "datos/glove_corpus.txt", "datos/embeddings_minilm.json");

        Scanner teclado = new Scanner(System.in);
        while (true) {
            System.out.print("\nIds de los artículos separados por coma (ej. 1, 4, 15), vacío para salir: ");
            String linea = teclado.nextLine();
            if (linea.trim().isEmpty()) {
                break;
            }
            try {
                List<Articulo> seleccion = ComparadorSimilitud.seleccionar(corpus, linea);
                ResultadoSimilitud resultado = comparador.comparar(seleccion);
                comparador.imprimir(resultado);
                comparador.guardarCsv(resultado, "resultados/similitud.csv");
                System.out.println("\nGuardado en resultados/similitud.csv");
            } catch (IllegalArgumentException error) {
                System.out.println("Error: " + error.getMessage());
            }
        }
    }
}