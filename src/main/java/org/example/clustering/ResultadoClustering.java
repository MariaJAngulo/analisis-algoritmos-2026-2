package org.example.clustering;

import java.util.List;

/** Resultado de agrupar con un enlace: fusiones, árbol y calidad (correlación cofenética). */
public class ResultadoClustering {

    public final Enlace enlace;
    public final List<Fusion> fusiones;
    public final Nodo raiz;
    public final double correlacionCofenetica;

    public ResultadoClustering(Enlace enlace, List<Fusion> fusiones, Nodo raiz, double correlacionCofenetica) {
        this.enlace = enlace;
        this.fusiones = fusiones;
        this.raiz = raiz;
        this.correlacionCofenetica = correlacionCofenetica;
    }
}
