package org.example;

import org.example.modelo.Articulo;
import org.example.modelo.Corpus;
import org.example.texto.Preprocesador;

import java.util.List;

/** Provisional (Fase 1): comprueba la carga del corpus. El menú llega en la Fase 5. */
public class Main {
    public static void main(String[] args) throws Exception {
        List<Articulo> corpus = Corpus.cargar("datos/articulos.json");
        System.out.println("Artículos cargados: " + corpus.size());
        for (Articulo a : corpus) {
            System.out.println(a.etiquetaCorta() + " | " + a.titulo + " | " + a.autoresTexto());
        }
        Preprocesador p = new Preprocesador();
        System.out.println("\nTokens del artículo 1: " + p.tokenizar(corpus.get(0).resumen).size());
        System.out.println("Normalizado (80 car.): " + p.normalizar(corpus.get(0).resumen).substring(0, 80));
    }
}
