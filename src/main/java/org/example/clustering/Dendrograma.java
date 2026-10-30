package org.example.clustering;

import org.example.modelo.Articulo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Dibuja el dendrograma como SVG escrito a mano (líneas y textos), sin librerías.
 * Eje vertical: un artículo por fila (etiqueta corta). Eje horizontal: altura de la fusión.
 * Posición Y de una hoja = su orden en el recorrido del árbol; la de un nodo interno =
 * promedio de las de sus hijos; posición X = altura de la fusión. O(n).
 */
public class Dendrograma {

    private static final int MARGEN_IZQ = 380;
    private static final int MARGEN_DER = 50;
    private static final int MARGEN_SUP = 50;
    private static final int MARGEN_INF = 70;
    private static final int ANCHO_ARBOL = 620;
    private static final int PASO = 26; // alto de cada fila

    /** Devuelve el SVG completo como texto (lo usan el archivo y, luego, la página web). */
    public static String generar(ResultadoClustering resultado, List<Articulo> articulos) {
        Nodo raiz = resultado.raiz;
        int n = articulos.size();

        // Orden de las hojas: recorrido izquierda-derecha del árbol
        List<Integer> orden = new ArrayList<>();
        recorrer(raiz, orden);
        Map<Integer, Integer> posicion = new HashMap<>();
        for (int i = 0; i < orden.size(); i++) {
            posicion.put(orden.get(i), i);
        }

        double alturaMax = raiz.altura > 0 ? raiz.altura : 1.0;
        // Todas las fusiones suelen ocurrir lejos de 0: el eje empieza cerca de la primera fusión
        // (redondeado a 0.05) para que la estructura no quede apretada a la derecha.
        double primeraFusion = resultado.fusiones.get(0).distancia;
        double ejeMin = Math.floor(primeraFusion * 20) / 20.0;
        if (ejeMin >= alturaMax) {
            ejeMin = 0.0;
        }
        int ancho = MARGEN_IZQ + ANCHO_ARBOL + MARGEN_DER;
        int alto = MARGEN_SUP + n * PASO + MARGEN_INF;

        StringBuilder svg = new StringBuilder();
        svg.append(String.format(Locale.US,
                "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 %d %d\" width=\"%d\" height=\"%d\" "
                        + "font-family=\"Arial, sans-serif\">%n", ancho, alto, ancho, alto));
        svg.append("<rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>\n");
        svg.append(String.format(Locale.US,
                "<text x=\"%d\" y=\"28\" font-size=\"16\" font-weight=\"bold\" fill=\"#1a1a1a\">"
                        + "Dendrograma - enlace %s (correlación cofenética = %.4f)</text>%n",
                20, escapar(resultado.enlace.nombre), resultado.correlacionCofenetica));

        dibujar(raiz, articulos, posicion, ejeMin, alturaMax, svg);
        dibujarEje(ejeMin, alturaMax, n, svg);

        svg.append("</svg>\n");
        return svg.toString();
    }

    public static void guardar(String svg, String ruta) throws IOException {
        java.io.File carpeta = new java.io.File(ruta).getAbsoluteFile().getParentFile();
        if (carpeta != null) {
            carpeta.mkdirs();
        }
        try (PrintWriter salida = new PrintWriter(ruta, "UTF-8")) {
            salida.print(svg);
        }
    }

    // Recorrido en orden: izquierda, derecha. Las hojas quedan en el orden en que se dibujan.
    private static void recorrer(Nodo nodo, List<Integer> orden) {
        if (nodo.esHoja()) {
            orden.add(nodo.id);
            return;
        }
        recorrer(nodo.izquierdo, orden);
        recorrer(nodo.derecho, orden);
    }

    // Las hojas (altura 0) quedan en el borde izquierdo del eje
    private static double x(double altura, double ejeMin, double alturaMax) {
        double h = Math.max(altura, ejeMin);
        return MARGEN_IZQ + (h - ejeMin) / (alturaMax - ejeMin) * ANCHO_ARBOL;
    }

    /** Dibuja el subárbol y devuelve la posición Y de su nodo. */
    private static double dibujar(Nodo nodo, List<Articulo> articulos, Map<Integer, Integer> posicion,
                                  double ejeMin, double alturaMax, StringBuilder svg) {
        if (nodo.esHoja()) {
            double y = MARGEN_SUP + posicion.get(nodo.id) * PASO + PASO / 2.0;
            svg.append(String.format(Locale.US,
                    "<text x=\"%.1f\" y=\"%.1f\" font-size=\"12\" text-anchor=\"end\" fill=\"#222\">%s</text>%n",
                    x(0, ejeMin, alturaMax) - 8, y + 4, escapar(articulos.get(nodo.id).etiquetaCorta())));
            return y;
        }
        double yIzq = dibujar(nodo.izquierdo, articulos, posicion, ejeMin, alturaMax, svg);
        double yDer = dibujar(nodo.derecho, articulos, posicion, ejeMin, alturaMax, svg);

        double xNodo = x(nodo.altura, ejeMin, alturaMax);
        double xIzq = x(nodo.izquierdo.altura, ejeMin, alturaMax);
        double xDer = x(nodo.derecho.altura, ejeMin, alturaMax);

        linea(svg, xIzq, yIzq, xNodo, yIzq); // brazo horizontal izquierdo
        linea(svg, xDer, yDer, xNodo, yDer); // brazo horizontal derecho
        linea(svg, xNodo, yIzq, xNodo, yDer); // unión vertical
        return (yIzq + yDer) / 2.0;
    }

    private static void dibujarEje(double ejeMin, double alturaMax, int n, StringBuilder svg) {
        double yEje = MARGEN_SUP + n * PASO + 8;
        svg.append(String.format(Locale.US,
                "<line x1=\"%.1f\" y1=\"%.1f\" x2=\"%.1f\" y2=\"%.1f\" stroke=\"#888\" stroke-width=\"1\"/>%n",
                x(ejeMin, ejeMin, alturaMax), yEje, x(alturaMax, ejeMin, alturaMax), yEje));
        for (int t = 0; t <= 5; t++) {
            double valor = ejeMin + (alturaMax - ejeMin) * t / 5.0;
            double px = x(valor, ejeMin, alturaMax);
            svg.append(String.format(Locale.US,
                    "<line x1=\"%.1f\" y1=\"%.1f\" x2=\"%.1f\" y2=\"%.1f\" stroke=\"#888\"/>%n",
                    px, yEje, px, yEje + 5));
            svg.append(String.format(Locale.US,
                    "<text x=\"%.1f\" y=\"%.1f\" font-size=\"11\" text-anchor=\"middle\" fill=\"#555\">%.3f</text>%n",
                    px, yEje + 19, valor));
        }
        String aviso = ejeMin > 0 ? String.format(Locale.US, " - el eje empieza en %.2f", ejeMin) : "";
        svg.append(String.format(Locale.US,
                "<text x=\"%.1f\" y=\"%.1f\" font-size=\"12\" text-anchor=\"middle\" fill=\"#333\">"
                        + "Altura de fusión (distancia = 1 - coseno TF-IDF)%s</text>%n",
                x((ejeMin + alturaMax) / 2, ejeMin, alturaMax), yEje + 40, aviso));
    }

    private static void linea(StringBuilder svg, double x1, double y1, double x2, double y2) {
        svg.append(String.format(Locale.US,
                "<line x1=\"%.1f\" y1=\"%.1f\" x2=\"%.1f\" y2=\"%.1f\" stroke=\"#1f3a5f\" stroke-width=\"1.6\"/>%n",
                x1, y1, x2, y2));
    }

    // Los títulos pueden traer & o < que romperían el XML del SVG
    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
