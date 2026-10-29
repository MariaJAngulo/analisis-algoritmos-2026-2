package org.example.similitud;

import org.example.modelo.Articulo;
import org.example.util.Medidor;

import java.util.List;

/** Datos de una comparación: los consume la consola, el CSV y (luego) la página web. */
public class ResultadoSimilitud {

    public final List<Articulo> articulos;
    public final Similitud[] algoritmos;
    /** matrices[a][i][j] = similitud del algoritmo a entre los artículos i y j (0 a 1). */
    public final double[][][] matrices;
    /** Distancia euclidiana, solo para los modelos de IA (mismo orden que nombresDistancia). */
    public final List<String> nombresDistancia;
    public final List<double[][]> distancias;
    /** Tiempo de calcular todos los pares de la selección, por algoritmo (ms). */
    public final Medidor.Estadistica[] tiempos;

    public ResultadoSimilitud(List<Articulo> articulos, Similitud[] algoritmos, double[][][] matrices,
                              List<String> nombresDistancia, List<double[][]> distancias,
                              Medidor.Estadistica[] tiempos) {
        this.articulos = articulos;
        this.algoritmos = algoritmos;
        this.matrices = matrices;
        this.nombresDistancia = nombresDistancia;
        this.distancias = distancias;
        this.tiempos = tiempos;
    }
}