package org.example.clustering;

import org.example.modelo.Articulo;
import org.example.similitud.CosenoTfIdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Requerimiento 2: agrupa los artículos con los 3 enlaces, dibuja los dendrogramas y decide
 * cuál agrupa mejor según la correlación cofenética.
 */
public class AnalisisClustering {

    private final List<Articulo> articulos;
    private final MatrizDistancias matriz;

    public AnalisisClustering(List<Articulo> articulos) {
        this.articulos = articulos;
        this.matriz = new MatrizDistancias(articulos, new CosenoTfIdf(articulos));
    }

    public List<Articulo> articulos() {
        return articulos;
    }

    /** Ejecuta los 3 enlaces sobre la misma matriz de distancias. */
    public List<ResultadoClustering> ejecutar() {
        ClusteringJerarquico algoritmo = new ClusteringJerarquico();
        List<ResultadoClustering> resultados = new ArrayList<>();
        for (Enlace enlace : Enlace.values()) {
            resultados.add(algoritmo.agrupar(matriz, enlace));
        }
        return resultados;
    }

    /** El de mayor correlación cofenética. */
    public ResultadoClustering ganador(List<ResultadoClustering> resultados) {
        ResultadoClustering mejor = resultados.get(0);
        for (ResultadoClustering r : resultados) {
            if (r.correlacionCofenetica > mejor.correlacionCofenetica) {
                mejor = r;
            }
        }
        return mejor;
    }

    /** Escribe resultados/dendrograma_simple.svg, _completo.svg y _promedio.svg. */
    public void guardarDendrogramas(List<ResultadoClustering> resultados, String carpeta) throws IOException {
        for (ResultadoClustering r : resultados) {
            String archivo = carpeta + "/dendrograma_" + r.enlace.name().toLowerCase(Locale.ROOT) + ".svg";
            Dendrograma.guardar(Dendrograma.generar(r, articulos), archivo);
        }
    }

    public void imprimir(List<ResultadoClustering> resultados) {
        for (ResultadoClustering r : resultados) {
            System.out.println("\n=== FUSIONES - ENLACE " + r.enlace.nombre.toUpperCase(Locale.ROOT) + " ===");
            for (Fusion f : r.fusiones) {
                System.out.printf(Locale.US, "Paso %2d: %-28s + %-28s -> distancia %.4f, tamaño %d%n",
                        f.paso, ids(f.hojasA), ids(f.hojasB), f.distancia, f.tamano);
            }
        }

        System.out.println("\n=== CORRELACIÓN COFENÉTICA (más cerca de 1 = mejor) ===");
        System.out.printf("%-22s %12s%n", "Enlace", "Correlación");
        for (ResultadoClustering r : resultados) {
            System.out.printf(Locale.US, "%-22s %12.4f%n", r.enlace.nombre, r.correlacionCofenetica);
        }
        ResultadoClustering mejor = ganador(resultados);
        System.out.printf(Locale.US, "%nMejor agrupación: enlace %s (%.4f): su dendrograma conserva mejor "
                + "las distancias originales entre artículos.%n", mejor.enlace.nombre, mejor.correlacionCofenetica);
    }

    // "{1, 4, 15}" con los ids de los artículos de un clúster
    public String ids(List<Integer> hojas) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < hojas.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(articulos.get(hojas.get(i)).id);
        }
        return sb.append("}").toString();
    }
}
