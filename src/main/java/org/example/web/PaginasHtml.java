package org.example.web;

import org.example.busqueda.ResultadoBusquedaPatron;
import org.example.clustering.AnalisisClustering;
import org.example.clustering.Dendrograma;
import org.example.clustering.Fusion;
import org.example.clustering.ResultadoClustering;
import org.example.modelo.Articulo;
import org.example.similitud.ResultadoSimilitud;
import org.example.util.Medidor;

import java.util.List;
import java.util.Locale;

/** Arma el HTML de cada página con StringBuilder (sin plantillas ni librerías). */
public class PaginasHtml {

    /** Escapa &, <, > y comillas: los títulos y autores pueden traerlos. */
    public static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String f(double valor, int decimales) {
        return String.format(Locale.US, "%." + decimales + "f", valor);
    }

    /** Estructura común: encabezado, hoja de estilo y contenido. */
    public static String envolver(String titulo, String cuerpo) {
        return "<!DOCTYPE html>\n<html lang=\"es\">\n<head>\n<meta charset=\"UTF-8\">\n"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
                + "<title>" + escapar(titulo) + "</title>\n<link rel=\"stylesheet\" href=\"/estilo.css\">\n</head>\n<body>\n"
                + "<header class=\"barra\"><div class=\"contenido\"><a class=\"marca\" href=\"/\">Análisis de algoritmos en bibliometría</a></div></header>\n<main>\n"
                + cuerpo + "\n</main>\n</body>\n</html>\n";
    }

    public static String error(String mensaje) {
        return envolver("Error", "<h1>No se pudo completar la solicitud</h1>\n<div class=\"error\">"
                + escapar(mensaje) + "</div>\n<p><a class=\"boton\" href=\"/\">Volver al inicio</a></p>");
    }

    /** Tabla de id, título y autores (se usa en el inicio y en los resultados). */
    public static String tablaArticulos(List<Articulo> articulos) {
        StringBuilder sb = new StringBuilder("<div class=\"tabla\"><table>\n<tr><th>Id</th><th>Título</th><th>Autores</th></tr>\n");
        for (Articulo a : articulos) {
            sb.append("<tr><td>").append(escapar(a.id)).append("</td><td>").append(escapar(a.titulo))
                    .append("</td><td>").append(escapar(a.autoresTexto())).append("</td></tr>\n");
        }
        return sb.append("</table></div>").toString();
    }

    // ------------------------------------------------------------- similitud

    public static String similitud(ResultadoSimilitud r) {
        int n = r.articulos.size();
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Similitud entre artículos</h1>\n");
        sb.append("<h2>Artículos seleccionados</h2>\n").append(tablaArticulos(r.articulos));

        sb.append("<h2>Matrices de similitud (0 a 1)</h2>\n<p class=\"nota\">1 = idénticos. Cada celda compara el resumen de la fila con el de la columna. El tono se escala dentro de cada matriz: más oscuro significa más parecidos que los demás pares.</p>\n");
        for (int k = 0; k < r.algoritmos.length; k++) {
            sb.append("<h3>").append(escapar(r.algoritmos[k].nombre())).append("</h3>\n");
            sb.append(matriz(r.articulos, r.matrices[k], 4, true));
        }

        sb.append("<h2>Distancia euclidiana (modelos de IA)</h2>\n<p class=\"nota\">Menor distancia = vectores más cercanos.</p>\n");
        for (int k = 0; k < r.distancias.size(); k++) {
            sb.append("<h3>").append(escapar(r.nombresDistancia.get(k))).append("</h3>\n");
            sb.append(matriz(r.articulos, r.distancias.get(k), 3, false));
        }

        sb.append("<h2>Resumen por par</h2>\n<div class=\"tabla\"><table>\n<tr><th>Par</th>");
        for (int k = 0; k < r.algoritmos.length; k++) {
            sb.append("<th class=\"num\">").append(escapar(r.algoritmos[k].nombre())).append("</th>");
        }
        sb.append("</tr>\n");
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                sb.append("<tr><td>").append(escapar(r.articulos.get(i).id)).append(" - ")
                        .append(escapar(r.articulos.get(j).id)).append("</td>");
                for (int k = 0; k < r.algoritmos.length; k++) {
                    sb.append("<td class=\"num\">").append(f(r.matrices[k][i][j], 4)).append("</td>");
                }
                sb.append("</tr>\n");
            }
        }
        sb.append("</table></div>\n");

        sb.append("<h2>Tiempo de calcular todos los pares (ms)</h2>\n");
        sb.append("<p class=\"nota\">Coseno TF-IDF, GloVe y MiniLM guardan los vectores en memoria, por eso su tiempo no incluye armarlos la primera vez.</p>\n");
        sb.append("<div class=\"tabla\"><table>\n<tr><th>Algoritmo</th><th class=\"num\">Promedio</th><th class=\"num\">Mínimo</th><th class=\"num\">Máximo</th><th class=\"num\">Desv.</th></tr>\n");
        for (int k = 0; k < r.algoritmos.length; k++) {
            Medidor.Estadistica e = r.tiempos[k];
            sb.append("<tr><td>").append(escapar(r.algoritmos[k].nombre())).append("</td>")
                    .append("<td class=\"num\">").append(f(e.promedioMs, 4)).append("</td>")
                    .append("<td class=\"num\">").append(f(e.minimoMs, 4)).append("</td>")
                    .append("<td class=\"num\">").append(f(e.maximoMs, 4)).append("</td>")
                    .append("<td class=\"num\">").append(f(e.desviacionMs, 4)).append("</td></tr>\n");
        }
        sb.append("</table></div>\n<p><a class=\"boton\" href=\"/\">Nueva consulta</a></p>");
        return envolver("Similitud", sb.toString());
    }

    private static String matriz(List<Articulo> articulos, double[][] m, int decimales, boolean sombrear) {
        int n = articulos.size();
        // Rango de los valores fuera de la diagonal: el tono se escala dentro de cada matriz
        double minimo = Double.MAX_VALUE;
        double maximo = -Double.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    minimo = Math.min(minimo, m[i][j]);
                    maximo = Math.max(maximo, m[i][j]);
                }
            }
        }
        double rango = maximo - minimo;

        StringBuilder sb = new StringBuilder("<div class=\"tabla\"><table>\n<tr><th></th>");
        for (Articulo a : articulos) {
            sb.append("<th class=\"num\" title=\"").append(escapar(a.titulo)).append("\">[").append(escapar(a.id)).append("]</th>");
        }
        sb.append("</tr>\n");
        for (int i = 0; i < n; i++) {
            sb.append("<tr><th title=\"").append(escapar(articulos.get(i).titulo)).append("\">[")
                    .append(escapar(articulos.get(i).id)).append("]</th>");
            for (int j = 0; j < n; j++) {
                String estilo = "";
                if (sombrear) {
                    if (i == j) {
                        estilo = " style=\"background:#e9edf4;color:#566174\""; // la diagonal siempre vale 1
                    } else {
                        double relativo = rango > 0 ? (m[i][j] - minimo) / rango : 0.5;
                        estilo = " style=\"background:rgba(43,76,140," + f(0.07 + 0.50 * relativo, 2) + ")\"";
                    }
                }
                sb.append("<td class=\"num\"").append(estilo).append(">").append(f(m[i][j], decimales)).append("</td>");
            }
            sb.append("</tr>\n");
        }
        return sb.append("</table></div>\n").toString();
    }

    // ------------------------------------------------------------ clustering

    public static String clustering(AnalisisClustering analisis, List<ResultadoClustering> resultados) {
        ResultadoClustering ganador = analisis.ganador(resultados);
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Clustering jerárquico</h1>\n");
        sb.append("<p class=\"nota\">Distancia entre artículos = 1 - coseno TF-IDF de sus resúmenes. Se agrupan los 20 artículos con tres criterios de enlace.</p>\n");

        sb.append("<h2>Correlación cofenética</h2>\n<p class=\"nota\">Mide cuánto conserva el dendrograma las distancias originales (más cerca de 1 = mejor).</p>\n");
        sb.append("<div class=\"tabla\"><table>\n<tr><th>Enlace</th><th class=\"num\">Correlación cofenética</th></tr>\n");
        for (ResultadoClustering r : resultados) {
            sb.append(r == ganador ? "<tr class=\"ganador\">" : "<tr>").append("<td>").append(escapar(r.enlace.nombre))
                    .append("</td><td class=\"num\">").append(f(r.correlacionCofenetica, 4)).append("</td></tr>\n");
        }
        sb.append("</table></div>\n<p class=\"ok\">Mejor agrupación: enlace ").append(escapar(ganador.enlace.nombre))
                .append(" (").append(f(ganador.correlacionCofenetica, 4)).append(").</p>\n");

        for (ResultadoClustering r : resultados) {
            sb.append("<h2>Enlace ").append(escapar(r.enlace.nombre)).append("</h2>\n");
            sb.append("<div class=\"svg\">\n").append(Dendrograma.generar(r, analisis.articulos())).append("</div>\n");
            sb.append("<details><summary>Ver las ").append(r.fusiones.size()).append(" fusiones</summary>\n");
            sb.append("<div class=\"tabla\"><table>\n<tr><th class=\"num\">Paso</th><th>Clúster A</th><th>Clúster B</th><th class=\"num\">Distancia</th><th class=\"num\">Tamaño</th></tr>\n");
            for (Fusion fu : r.fusiones) {
                sb.append("<tr><td class=\"num\">").append(fu.paso).append("</td><td>").append(analisis.ids(fu.hojasA))
                        .append("</td><td>").append(analisis.ids(fu.hojasB)).append("</td><td class=\"num\">")
                        .append(f(fu.distancia, 4)).append("</td><td class=\"num\">").append(fu.tamano).append("</td></tr>\n");
            }
            sb.append("</table></div></details>\n");
        }
        sb.append("<h2>Artículos</h2>\n").append(tablaArticulos(analisis.articulos()));
        sb.append("<p><a class=\"boton\" href=\"/\">Volver al inicio</a></p>");
        return envolver("Clustering jerárquico", sb.toString());
    }

    // -------------------------------------------------------------- búsqueda

    public static String busqueda(ResultadoBusquedaPatron r) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Búsqueda del patrón «").append(escapar(r.patron)).append("»</h1>\n");

        int conCoincidencias = 0;
        StringBuilder filas = new StringBuilder();
        for (int i = 0; i < r.articulos.size(); i++) {
            List<Integer> pos = r.posiciones.get(i);
            if (pos.isEmpty()) {
                continue;
            }
            conCoincidencias++;
            Articulo a = r.articulos.get(i);
            filas.append("<tr><td>").append(escapar(a.id)).append("</td><td>").append(escapar(a.titulo))
                    .append("</td><td>").append(escapar(a.autoresTexto())).append("</td><td class=\"num\">")
                    .append(pos.size()).append("</td><td class=\"monosp\">").append(posicionesCortas(pos)).append("</td></tr>\n");
        }
        sb.append("<p class=\"lead\">").append(r.coincidencias[0]).append(r.coincidencias[0] == 1 ? " coincidencia" : " coincidencias")
                .append(" en ").append(conCoincidencias).append(" de los ").append(r.articulos.size())
                .append(" artículos.</p>\n<p class=\"nota\">Las posiciones se cuentan sobre el resumen en minúsculas, desde 0.</p>\n");
        if (conCoincidencias > 0) {
            sb.append("<div class=\"tabla\"><table>\n<tr><th>Id</th><th>Título</th><th>Autores</th><th class=\"num\">Ocurrencias</th><th>Posiciones</th></tr>\n")
                    .append(filas).append("</table></div>\n");
        }

        sb.append("<h2>Comparación de algoritmos (los 20 resúmenes)</h2>\n");
        sb.append("<div class=\"tabla\"><table>\n<tr><th>Algoritmo</th><th class=\"num\">Coincidencias</th><th class=\"num\">Comparaciones</th>"
                + "<th class=\"num\">Colisiones</th><th class=\"num\">Preproc. (µs)</th><th class=\"num\">Búsqueda (µs)</th><th class=\"num\">Total ms (prom ± desv.)</th></tr>\n");
        for (int a = 0; a < r.algoritmos.length; a++) {
            Medidor.Estadistica e = r.tiempoTotal[a];
            sb.append("<tr><td>").append(escapar(r.algoritmos[a])).append("</td><td class=\"num\">").append(r.coincidencias[a])
                    .append("</td><td class=\"num\">").append(r.comparaciones[a]).append("</td><td class=\"num\">")
                    .append(r.colisiones[a]).append("</td><td class=\"num\">").append(f(r.preprocesamientoNs[a] / 1000.0, 2))
                    .append("</td><td class=\"num\">").append(f(r.busquedaNs[a] / 1000.0, 2)).append("</td><td class=\"num\">")
                    .append(f(e.promedioMs, 4)).append(" ± ").append(f(e.desviacionMs, 4)).append("</td></tr>\n");
        }
        sb.append("</table></div>\n");
        sb.append(r.mismasPosiciones
                ? "<p class=\"ok\">Verificación: los 3 algoritmos encontraron exactamente las mismas posiciones.</p>\n"
                : "<p class=\"aviso\">Atención: los algoritmos no coinciden en las posiciones encontradas.</p>\n");
        sb.append("<p class=\"nota\">Rabin-Karp cuenta solo las comparaciones de verificación, no las del hash.</p>\n");
        sb.append("<p><a class=\"boton\" href=\"/\">Nueva búsqueda</a></p>");
        return envolver("Búsqueda de patrones", sb.toString());
    }

    private static String posicionesCortas(List<Integer> pos) {
        int limite = 15;
        if (pos.size() <= limite) {
            return pos.toString();
        }
        return pos.subList(0, limite) + " … (+" + (pos.size() - limite) + " más)";
    }
}
