package org.example.busqueda;

import java.util.List;

/** Lo que devuelve un algoritmo al buscar un patrón en UN texto. */
public class ResultadoBusqueda {

    /** Posiciones (desde 0) donde empieza cada coincidencia, incluyendo las que se solapan. */
    public final List<Integer> posiciones;
    /** Comparaciones de caracteres hechas en la fase de búsqueda (texto vs patrón). */
    public final long comparaciones;
    /** Rabin-Karp: veces que el hash coincidió pero el texto era distinto (en los demás, 0). */
    public final long colisiones;
    public final long preprocesamientoNs;
    public final long busquedaNs;

    public ResultadoBusqueda(List<Integer> posiciones, long comparaciones, long colisiones,
                             long preprocesamientoNs, long busquedaNs) {
        this.posiciones = posiciones;
        this.comparaciones = comparaciones;
        this.colisiones = colisiones;
        this.preprocesamientoNs = preprocesamientoNs;
        this.busquedaNs = busquedaNs;
    }
}
