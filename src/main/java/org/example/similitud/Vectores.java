package org.example.similitud;

/** Operaciones entre vectores densos (embeddings), hechas a mano. */
public class Vectores {

    /** Coseno = (a·b) / (|a|·|b|). Devuelve 0 si algún vector es nulo. O(d). */
    public static double coseno(double[] a, double[] b) {
        double producto = 0;
        double normaA = 0;
        double normaB = 0;
        for (int i = 0; i < a.length; i++) {
            producto += a[i] * b[i];
            normaA += a[i] * a[i];
            normaB += b[i] * b[i];
        }
        if (normaA == 0 || normaB == 0) {
            return 0.0;
        }
        return producto / (Math.sqrt(normaA) * Math.sqrt(normaB));
    }

    /** Distancia euclidiana = √Σ(a_i - b_i)². O(d). */
    public static double distanciaEuclidiana(double[] a, double[] b) {
        double suma = 0;
        for (int i = 0; i < a.length; i++) {
            double diferencia = a[i] - b[i];
            suma += diferencia * diferencia;
        }
        return Math.sqrt(suma);
    }
}