package org.example.similitud;

import org.example.modelo.Articulo;

/**
 * Base común de los dos modelos de IA: cada uno solo decide cómo obtener
 * el vector de un abstract; la similitud (coseno) y la distancia euclidiana
 * se calculan igual para ambos.
 */
public abstract class Embedding implements Similitud {

    /** Vector del abstract completo del artículo. */
    public abstract double[] vectorDe(Articulo articulo);

    /** Coseno entre los vectores. Se limita a [0, 1] para cumplir el contrato de Similitud. */
    @Override
    public double calcular(Articulo a, Articulo b) {
        double coseno = Vectores.coseno(vectorDe(a), vectorDe(b));
        return Math.max(0.0, Math.min(1.0, coseno));
    }

    /** Distancia euclidiana entre los vectores (se muestra junto a la similitud). */
    public double distanciaEuclidiana(Articulo a, Articulo b) {
        return Vectores.distanciaEuclidiana(vectorDe(a), vectorDe(b));
    }
}