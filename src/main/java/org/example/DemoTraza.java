package org.example;

import org.example.busqueda.BoyerMoore;
import org.example.busqueda.BuscadorPatron;
import org.example.busqueda.Kmp;
import org.example.busqueda.RabinKarp;
import org.example.modelo.Articulo;
import org.example.modelo.Corpus;
import org.example.texto.Preprocesador;

import java.util.Scanner;

/**
 * Demostración paso a paso para el documento técnico (no forma parte del menú principal).
 * Toma un fragmento corto de un abstract y muestra la traza de cada algoritmo de búsqueda.
 */
public class DemoTraza {

    private static final int LARGO_MAXIMO = 100;

    public static void main(String[] args) throws Exception {
        Corpus corpus = Corpus.desde("datos/articulos.json");
        Preprocesador preprocesador = new Preprocesador();
        Scanner teclado = new Scanner(System.in);

        System.out.print("Patrón (ej. learning): ");
        String patron = preprocesador.normalizar(teclado.nextLine());
        System.out.print("Id del artículo (ej. 11): ");
        Articulo articulo = corpus.buscarPorId(teclado.nextLine().trim());
        if (articulo == null || patron.isEmpty()) {
            System.out.println("Patrón vacío o artículo inexistente.");
            return;
        }
        String texto = preprocesador.normalizar(articulo.resumen);
        System.out.print("Posición de inicio del fragmento (0 = principio): ");
        int inicio = Math.max(0, Math.min(Integer.parseInt(teclado.nextLine().trim()), texto.length() - 1));
        System.out.print("Largo del fragmento (máx. " + LARGO_MAXIMO + "): ");
        int largo = Math.min(Integer.parseInt(teclado.nextLine().trim()), LARGO_MAXIMO);
        String fragmento = texto.substring(inicio, Math.min(texto.length(), inicio + largo));

        System.out.println("\nFragmento (" + fragmento.length() + " caracteres) del artículo [" + articulo.id + "]:");
        System.out.println(fragmento + "\n");

        BuscadorPatron[] buscadores = {new Kmp(), new BoyerMoore(), new RabinKarp()};
        for (BuscadorPatron buscador : buscadores) {
            buscador.imprimirTraza(fragmento, patron);
            System.out.println();
        }
    }
}
