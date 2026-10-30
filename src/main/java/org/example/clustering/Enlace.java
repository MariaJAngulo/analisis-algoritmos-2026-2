package org.example.clustering;

/** Cómo se mide la distancia entre dos clústeres a partir de las distancias entre sus miembros. */
public enum Enlace {
    SIMPLE("Simple (single)"),      // la MÍNIMA distancia entre miembros
    COMPLETO("Completo (complete)"), // la MÁXIMA distancia entre miembros
    PROMEDIO("Promedio (average)");  // el PROMEDIO de todas las distancias entre miembros

    public final String nombre;

    Enlace(String nombre) {
        this.nombre = nombre;
    }
}
