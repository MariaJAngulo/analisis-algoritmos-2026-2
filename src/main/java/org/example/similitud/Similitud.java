package org.example.similitud;

import org.example.modelo.Articulo;

/** Contrato común de los 6 algoritmos de similitud entre abstracts. */
public interface Similitud {

    String nombre();

    /** Devuelve un valor entre 0 y 1 (1 = idénticos). */
    double calcular(Articulo a, Articulo b);
}
