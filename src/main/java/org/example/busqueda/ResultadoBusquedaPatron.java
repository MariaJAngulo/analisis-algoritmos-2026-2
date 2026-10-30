package org.example.busqueda;

import org.example.modelo.Articulo;
import org.example.util.Medidor;

import java.util.List;

/** Datos de una búsqueda sobre los 20 abstracts: los consumen la consola, el CSV y la web. */
public class ResultadoBusquedaPatron {

    public final String patron;
    public final List<Articulo> articulos;
    /** Posiciones de cada artículo (las de KMP; se verifica que las 3 sean iguales). */
    public final List<List<Integer>> posiciones;
    public final String[] algoritmos;
    public final long[] coincidencias;
    public final long[] comparaciones;
    public final long[] colisiones;
    /** Promedio, por repetición completa (los 20 abstracts), de cada fase, en nanosegundos. */
    public final double[] preprocesamientoNs;
    public final double[] busquedaNs;
    public final Medidor.Estadistica[] tiempoTotal;
    public final boolean mismasPosiciones;

    public ResultadoBusquedaPatron(String patron, List<Articulo> articulos, List<List<Integer>> posiciones,
                                   String[] algoritmos, long[] coincidencias, long[] comparaciones,
                                   long[] colisiones, double[] preprocesamientoNs, double[] busquedaNs,
                                   Medidor.Estadistica[] tiempoTotal, boolean mismasPosiciones) {
        this.patron = patron;
        this.articulos = articulos;
        this.posiciones = posiciones;
        this.algoritmos = algoritmos;
        this.coincidencias = coincidencias;
        this.comparaciones = comparaciones;
        this.colisiones = colisiones;
        this.preprocesamientoNs = preprocesamientoNs;
        this.busquedaNs = busquedaNs;
        this.tiempoTotal = tiempoTotal;
        this.mismasPosiciones = mismasPosiciones;
    }
}
