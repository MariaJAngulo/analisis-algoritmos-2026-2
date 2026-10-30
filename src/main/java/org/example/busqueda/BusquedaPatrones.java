package org.example.busqueda;

import org.example.modelo.Articulo;
import org.example.texto.Preprocesador;
import org.example.util.Medidor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Requerimiento 3: busca un patrón en los abstracts de todos los artículos con KMP,
 * Boyer-Moore y Rabin-Karp, y compara resultados, comparaciones y tiempos.
 * Texto y patrón se pasan a minúsculas (Preprocesador.normalizar) y las posiciones se
 * cuentan sobre ese texto normalizado.
 */
public class BusquedaPatrones {

    private static final int CALENTAMIENTO = 30;
    private static final int REPETICIONES = 100;

    private final List<Articulo> articulos;
    private final char[][] textos; // abstracts normalizados, uno por artículo
    private final BuscadorPatron[] buscadores = {new Kmp(), new BoyerMoore(), new RabinKarp()};
    private final Preprocesador preprocesador = new Preprocesador();

    public BusquedaPatrones(List<Articulo> articulos) {
        this.articulos = articulos;
        this.textos = new char[articulos.size()][];
        for (int i = 0; i < textos.length; i++) {
            textos[i] = preprocesador.normalizar(articulos.get(i).resumen).toCharArray();
        }
    }

    public ResultadoBusquedaPatron buscar(String patronEscrito) {
        String patron = preprocesador.normalizar(patronEscrito);
        if (patron.isEmpty()) {
            throw new IllegalArgumentException("Escribe un patrón para buscar (ej. learning).");
        }
        char[] p = patron.toCharArray();
        int nArticulos = articulos.size();
        int nAlgoritmos = buscadores.length;

        // Una pasada por algoritmo: posiciones, comparaciones y colisiones
        ResultadoBusqueda[][] r = new ResultadoBusqueda[nAlgoritmos][nArticulos];
        long[] coincidencias = new long[nAlgoritmos];
        long[] comparaciones = new long[nAlgoritmos];
        long[] colisiones = new long[nAlgoritmos];
        String[] nombres = new String[nAlgoritmos];
        for (int a = 0; a < nAlgoritmos; a++) {
            nombres[a] = buscadores[a].nombre();
            for (int i = 0; i < nArticulos; i++) {
                r[a][i] = buscadores[a].buscar(textos[i], p);
                coincidencias[a] += r[a][i].posiciones.size();
                comparaciones[a] += r[a][i].comparaciones;
                colisiones[a] += r[a][i].colisiones;
            }
        }

        // Los tres deben encontrar exactamente las mismas posiciones en cada artículo
        boolean iguales = true;
        List<List<Integer>> posiciones = new ArrayList<>();
        for (int i = 0; i < nArticulos; i++) {
            posiciones.add(r[0][i].posiciones);
            for (int a = 1; a < nAlgoritmos; a++) {
                if (!r[a][i].posiciones.equals(r[0][i].posiciones)) {
                    iguales = false;
                }
            }
        }

        // Tiempos: cada repetición recorre los 20 abstracts y suma preprocesamiento y búsqueda por separado
        double[] preNs = new double[nAlgoritmos];
        double[] busNs = new double[nAlgoritmos];
        Medidor.Estadistica[] total = new Medidor.Estadistica[nAlgoritmos];
        for (int a = 0; a < nAlgoritmos; a++) {
            BuscadorPatron buscador = buscadores[a];
            List<long[]> corridas = new ArrayList<>(); // {preprocesamiento, búsqueda} de cada repetición
            total[a] = Medidor.medir(() -> {
                long pre = 0;
                long bus = 0;
                for (int i = 0; i < nArticulos; i++) {
                    ResultadoBusqueda res = buscador.buscar(textos[i], p);
                    pre += res.preprocesamientoNs;
                    bus += res.busquedaNs;
                }
                corridas.add(new long[]{pre, bus});
                return pre + bus;
            }, CALENTAMIENTO, REPETICIONES);

            // Las primeras corridas son de calentamiento: solo se promedian las medidas
            for (int c = CALENTAMIENTO; c < corridas.size(); c++) {
                preNs[a] += corridas.get(c)[0];
                busNs[a] += corridas.get(c)[1];
            }
            preNs[a] /= REPETICIONES;
            busNs[a] /= REPETICIONES;
        }

        return new ResultadoBusquedaPatron(patron, articulos, posiciones, nombres, coincidencias,
                comparaciones, colisiones, preNs, busNs, total, iguales);
    }

    // ---------------------------------------------------------------- consola

    public void imprimir(ResultadoBusquedaPatron r) {
        System.out.println("\n=== BÚSQUEDA DEL PATRÓN \"" + r.patron + "\" ===");
        int conCoincidencias = 0;
        for (int i = 0; i < r.articulos.size(); i++) {
            List<Integer> pos = r.posiciones.get(i);
            if (pos.isEmpty()) {
                continue;
            }
            conCoincidencias++;
            Articulo a = r.articulos.get(i);
            System.out.println("\n[" + a.id + "] " + a.titulo);
            System.out.println("     Autores: " + a.autoresTexto());
            System.out.println("     Ocurrencias: " + pos.size() + "  Posiciones: " + posicionesCortas(pos));
        }
        System.out.println("\nArtículos con coincidencias: " + conCoincidencias + " de " + r.articulos.size());

        System.out.println("\n=== COMPARACIÓN DE ALGORITMOS (los 20 abstracts) ===");
        System.out.printf("%-12s %13s %14s %11s %16s %14s %20s%n", "Algoritmo", "Coincidencias",
                "Comparaciones", "Colisiones", "Preproc. (µs)", "Búsqueda (µs)", "Total ms (prom ± d.)");
        for (int a = 0; a < r.algoritmos.length; a++) {
            Medidor.Estadistica e = r.tiempoTotal[a];
            System.out.printf(Locale.US, "%-12s %13d %14d %11d %16.2f %14.2f %12.4f ± %.4f%n", r.algoritmos[a],
                    r.coincidencias[a], r.comparaciones[a], r.colisiones[a], r.preprocesamientoNs[a] / 1000.0,
                    r.busquedaNs[a] / 1000.0, e.promedioMs, e.desviacionMs);
        }
        System.out.println(r.mismasPosiciones
                ? "Verificación: los 3 algoritmos encontraron exactamente las mismas posiciones."
                : "ATENCIÓN: los algoritmos NO coinciden en las posiciones encontradas.");
        System.out.println("(Rabin-Karp cuenta solo las comparaciones de verificación, no las del hash.)");
    }

    private String posicionesCortas(List<Integer> pos) {
        int limite = 12;
        if (pos.size() <= limite) {
            return pos.toString();
        }
        return pos.subList(0, limite).toString() + " ... (+" + (pos.size() - limite) + " más)";
    }

    // -------------------------------------------------------------------- CSV

    /** Un renglón por artículo con coincidencias y, al final, la tabla de algoritmos. */
    public void guardarCsv(ResultadoBusquedaPatron r, String ruta) throws IOException {
        java.io.File carpeta = new java.io.File(ruta).getAbsoluteFile().getParentFile();
        if (carpeta != null) {
            carpeta.mkdirs();
        }
        try (PrintWriter csv = new PrintWriter(ruta, "UTF-8")) {
            csv.println("patron,id,titulo,autores,ocurrencias,posiciones");
            for (int i = 0; i < r.articulos.size(); i++) {
                List<Integer> pos = r.posiciones.get(i);
                if (pos.isEmpty()) {
                    continue;
                }
                Articulo a = r.articulos.get(i);
                StringBuilder lista = new StringBuilder();
                for (int k = 0; k < pos.size(); k++) {
                    lista.append(k > 0 ? " " : "").append(pos.get(k));
                }
                csv.println(campo(r.patron) + "," + campo(a.id) + "," + campo(a.titulo) + ","
                        + campo(a.autoresTexto()) + "," + pos.size() + "," + campo(lista.toString()));
            }
            csv.println();
            csv.println("algoritmo,coincidencias,comparaciones,colisiones,preprocesamiento_ns,busqueda_ns,"
                    + "total_promedio_ms,total_desviacion_ms,repeticiones");
            for (int a = 0; a < r.algoritmos.length; a++) {
                Medidor.Estadistica e = r.tiempoTotal[a];
                csv.println(String.format(Locale.US, "%s,%d,%d,%d,%.1f,%.1f,%.6f,%.6f,%d", campo(r.algoritmos[a]),
                        r.coincidencias[a], r.comparaciones[a], r.colisiones[a], r.preprocesamientoNs[a],
                        r.busquedaNs[a], e.promedioMs, e.desviacionMs, e.repeticiones));
            }
        }
    }

    private String campo(String texto) {
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }
}
