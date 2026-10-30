package org.example.clustering;

import java.util.ArrayList;
import java.util.List;

/**
 * Clustering jerárquico aglomerativo, hecho a mano (versión directa, O(n³)).
 *
 * 1. Cada artículo empieza como un clúster de un solo elemento (n clústeres).
 * 2. Se repite n-1 veces: buscar el par de clústeres más cercano según el enlace,
 *    unirlos en un clúster nuevo y registrar la fusión.
 * Cada búsqueda revisa todos los pares de artículos (O(n²)) y hay n-1 búsquedas: O(n³).
 * Con n = 20 es instantáneo y mucho más fácil de explicar que la fórmula de Lance-Williams.
 */
public class ClusteringJerarquico {

    public ResultadoClustering agrupar(MatrizDistancias matriz, Enlace enlace) {
        int n = matriz.tamano();

        // Paso 1: n clústeres de un elemento (hojas del árbol)
        List<Nodo> activos = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            List<Integer> hoja = new ArrayList<>();
            hoja.add(i);
            activos.add(new Nodo(i, null, null, 0.0, hoja));
        }

        List<Fusion> fusiones = new ArrayList<>();
        int siguienteId = n;

        // Paso 2: n-1 fusiones
        while (activos.size() > 1) {
            int mejorA = -1;
            int mejorB = -1;
            double mejorDistancia = Double.MAX_VALUE;

            // Buscar el par de clústeres más cercano (si hay empate, gana el primero encontrado)
            for (int a = 0; a < activos.size(); a++) {
                for (int b = a + 1; b < activos.size(); b++) {
                    double distancia = distanciaEntre(activos.get(a), activos.get(b), matriz, enlace);
                    if (distancia < mejorDistancia) {
                        mejorDistancia = distancia;
                        mejorA = a;
                        mejorB = b;
                    }
                }
            }

            // Unir los dos clústeres en uno nuevo
            Nodo izquierdo = activos.get(mejorA);
            Nodo derecho = activos.get(mejorB);
            List<Integer> hojas = new ArrayList<>(izquierdo.hojas);
            hojas.addAll(derecho.hojas);
            Nodo union = new Nodo(siguienteId++, izquierdo, derecho, mejorDistancia, hojas);

            fusiones.add(new Fusion(fusiones.size() + 1, izquierdo.hojas, derecho.hojas,
                    mejorDistancia, hojas.size()));

            activos.remove(mejorB); // primero el índice mayor para no mover el menor
            activos.remove(mejorA);
            activos.add(union);
        }

        Nodo raiz = activos.get(0);
        double correlacion = Cofenetica.correlacion(matriz, raiz);
        return new ResultadoClustering(enlace, fusiones, raiz, correlacion);
    }

    /** Distancia entre dos clústeres según el enlace. Recorre todos los pares de miembros. */
    private double distanciaEntre(Nodo x, Nodo y, MatrizDistancias matriz, Enlace enlace) {
        double minimo = Double.MAX_VALUE;
        double maximo = 0.0;
        double suma = 0.0;
        for (int i : x.hojas) {
            for (int j : y.hojas) {
                double d = matriz.get(i, j);
                minimo = Math.min(minimo, d);
                maximo = Math.max(maximo, d);
                suma += d;
            }
        }
        switch (enlace) {
            case SIMPLE:
                return minimo;
            case COMPLETO:
                return maximo;
            default:
                return suma / ((double) x.hojas.size() * y.hojas.size());
        }
    }
}
