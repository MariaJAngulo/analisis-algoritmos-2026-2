package org.example.modelo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Carga el corpus de artículos desde datos/articulos.json (Jackson). */
public class Corpus {

    private final List<Articulo> articulos;

    public Corpus(List<Articulo> articulos) {
        this.articulos = articulos;
    }

    /** Lee el JSON { "papers": [ {id, title, authors, Abstract}, ... ] }. Costo O(N). */
    public static List<Articulo> cargar(String ruta) throws IOException {
        JsonNode raiz = new ObjectMapper().readTree(new File(ruta));
        JsonNode papers = raiz.get("papers");

        List<Articulo> lista = new ArrayList<>();
        for (JsonNode p : papers) {
            JsonNode nodoAutores = p.get("authors");
            String[] autores = new String[nodoAutores.size()];
            for (int i = 0; i < autores.length; i++) {
                autores[i] = nodoAutores.get(i).asText();
            }
            lista.add(new Articulo(p.get("id").asText(), p.get("title").asText(),
                    autores, p.get("Abstract").asText()));
        }
        return lista;
    }

    /** Atajo: crea un Corpus listo para buscar por id. */
    public static Corpus desde(String ruta) throws IOException {
        return new Corpus(cargar(ruta));
    }

    public List<Articulo> articulos() {
        return articulos;
    }

    /** Devuelve el artículo con ese id, o null si no existe. Costo O(N). */
    public Articulo buscarPorId(String id) {
        for (Articulo a : articulos) {
            if (a.id.equals(id)) {
                return a;
            }
        }
        return null;
    }
}
