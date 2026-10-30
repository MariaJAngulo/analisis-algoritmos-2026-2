package org.example.clustering;

import java.util.List;

/** Un paso del algoritmo: qué dos clústeres se unieron, a qué distancia y de qué tamaño quedó. */
public class Fusion {

    public final int paso;
    public final List<Integer> hojasA;
    public final List<Integer> hojasB;
    public final double distancia;
    public final int tamano;

    public Fusion(int paso, List<Integer> hojasA, List<Integer> hojasB, double distancia, int tamano) {
        this.paso = paso;
        this.hojasA = hojasA;
        this.hojasB = hojasB;
        this.distancia = distancia;
        this.tamano = tamano;
    }
}
