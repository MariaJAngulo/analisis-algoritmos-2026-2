package org.example.busqueda;

/**
 * Contrato común de los 3 algoritmos. Reciben el texto y el patrón ya normalizados
 * (minúsculas) como arreglos de caracteres: no se usa indexOf, contains, startsWith,
 * matches, regionMatches ni expresiones regulares.
 */
public interface BuscadorPatron {

    String nombre();

    ResultadoBusqueda buscar(char[] texto, char[] patron);

    /** Muestra paso a paso el funcionamiento sobre un texto corto (para la demostración). */
    void imprimirTraza(String texto, String patron);
}
