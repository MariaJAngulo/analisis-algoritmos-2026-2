package org.example.modelo;

/**
 * Un artículo del corpus: id, título, autores y resumen (abstract).
 * El título y los autores se usan para el "cruce de información"
 * en los resultados de similitud, clustering y búsqueda.
 */
public class Articulo {

    public final String id;
    public final String titulo;
    public final String[] autores;
    public final String resumen;

    public Articulo(String id, String titulo, String[] autores, String resumen) {
        this.id = id;
        this.titulo = titulo;
        this.autores = autores;
        this.resumen = resumen;
    }

    /** Autores separados por coma, para tablas y consola. */
    public String autoresTexto() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < autores.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(autores[i]);
        }
        return sb.toString();
    }

    /** Etiqueta corta: "[id] primeras 5 palabras del título..." (tablas y dendrogramas). */
    public String etiquetaCorta() {
        String[] palabras = titulo.trim().split("\\s+");
        StringBuilder sb = new StringBuilder("[" + id + "] ");
        int limite = Math.min(5, palabras.length);
        for (int i = 0; i < limite; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(palabras[i]);
        }
        if (palabras.length > 5) {
            sb.append("...");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return etiquetaCorta();
    }
}
