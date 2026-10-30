package org.example.clustering;

import java.util.List;

/**
 * Nodo del árbol del dendrograma. Una hoja es un artículo (id = su posición 0..n-1);
 * un nodo interno es la unión de dos clústeres (id = n, n+1, ...), con la altura a la
 * que se unieron y la lista de artículos (hojas) que contiene.
 */
public class Nodo {

    public final int id;
    public final Nodo izquierdo;
    public final Nodo derecho;
    public final double altura;
    public final List<Integer> hojas;

    public Nodo(int id, Nodo izquierdo, Nodo derecho, double altura, List<Integer> hojas) {
        this.id = id;
        this.izquierdo = izquierdo;
        this.derecho = derecho;
        this.altura = altura;
        this.hojas = hojas;
    }

    public boolean esHoja() {
        return izquierdo == null;
    }
}
