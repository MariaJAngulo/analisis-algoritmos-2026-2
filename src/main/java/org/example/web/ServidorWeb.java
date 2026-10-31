package org.example.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.example.busqueda.BusquedaPatrones;
import org.example.clustering.AnalisisClustering;
import org.example.clustering.ResultadoClustering;
import org.example.modelo.Articulo;
import org.example.modelo.Corpus;
import org.example.similitud.ComparadorSimilitud;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Interfaz web mínima con el HttpServer que ya trae el JDK (sin frameworks).
 * Rutas: /  /similitud?ids=1,2,3  /clustering  /buscar?patron=...
 * El puerto sale de la variable de entorno PORT (8081 por defecto: el 8080 suele estar ocupado) y la carpeta de datos
 * de DATOS_DIR ("datos" por defecto). Cada ruta llama a las mismas clases que el menú de consola.
 * Las solicitudes se atienden de una en una: los vectores se guardan en mapas que no son
 * seguros para varios hilos, y para este uso basta.
 */
public class ServidorWeb {

    private final Corpus corpus;
    private final ComparadorSimilitud comparador;
    private final AnalisisClustering clustering;
    private final BusquedaPatrones busqueda;
    private String paginaClustering; // se arma la primera vez y se reutiliza

    public ServidorWeb(String carpetaDatos) throws IOException {
        corpus = Corpus.desde(carpetaDatos + "/articulos.json");
        comparador = new ComparadorSimilitud(corpus.articulos(),
                carpetaDatos + "/glove_corpus.txt", carpetaDatos + "/embeddings_minilm.json");
        clustering = new AnalisisClustering(corpus.articulos());
        busqueda = new BusquedaPatrones(corpus.articulos());
    }

    public static void main(String[] args) throws Exception {
        String variablePuerto = System.getenv("PORT");
        int puerto = variablePuerto == null || variablePuerto.isBlank() ? 8081 : Integer.parseInt(variablePuerto.trim());
        String variableDatos = System.getenv("DATOS_DIR");
        String carpetaDatos = variableDatos == null || variableDatos.isBlank() ? "datos" : variableDatos.trim();

        ServidorWeb aplicacion = new ServidorWeb(carpetaDatos);
        HttpServer servidor = HttpServer.create(new InetSocketAddress(puerto), 0);
        servidor.createContext("/", aplicacion::atender);
        servidor.setExecutor(Executors.newSingleThreadExecutor());
        servidor.start();
        System.out.println("Servidor listo en http://localhost:" + puerto + "  (datos: " + carpetaDatos + ")");
    }

    /** Un solo punto de entrada: según la ruta, arma la página y la envía. */
    private void atender(HttpExchange intercambio) throws IOException {
        try {
            if (!intercambio.getRequestMethod().equals("GET")) {
                enviar(intercambio, 405, "text/html; charset=utf-8", PaginasHtml.error("Solo se permite GET."));
                return;
            }
            String ruta = intercambio.getRequestURI().getPath();
            Map<String, String> parametros = parametros(intercambio.getRequestURI());

            if (ruta.equals("/") || ruta.equals("/index.html")) {
                String plantilla = leerRecurso("/index.html");
                String pagina = plantilla.replace("<!--ARTICULOS-->", PaginasHtml.tablaArticulos(corpus.articulos()));
                enviar(intercambio, 200, "text/html; charset=utf-8", pagina);
            } else if (ruta.equals("/estilo.css")) {
                enviar(intercambio, 200, "text/css; charset=utf-8", leerRecurso("/estilo.css"));
            } else if (ruta.equals("/similitud")) {
                enviar(intercambio, 200, "text/html; charset=utf-8", similitud(parametros.getOrDefault("ids", "")));
            } else if (ruta.equals("/clustering")) {
                enviar(intercambio, 200, "text/html; charset=utf-8", clustering());
            } else if (ruta.equals("/buscar")) {
                enviar(intercambio, 200, "text/html; charset=utf-8",
                        PaginasHtml.busqueda(busqueda.buscar(parametros.getOrDefault("patron", ""))));
            } else {
                enviar(intercambio, 404, "text/html; charset=utf-8", PaginasHtml.error("No existe la página " + ruta));
            }
        } catch (IllegalArgumentException error) { // entrada inválida del usuario
            enviar(intercambio, 400, "text/html; charset=utf-8", PaginasHtml.error(error.getMessage()));
        } catch (Exception error) {
            error.printStackTrace();
            enviar(intercambio, 500, "text/html; charset=utf-8", PaginasHtml.error("Ocurrió un error interno."));
        }
    }

    private String similitud(String ids) {
        List<Articulo> seleccion = ComparadorSimilitud.seleccionar(corpus, ids);
        return PaginasHtml.similitud(comparador.comparar(seleccion));
    }

    private String clustering() {
        if (paginaClustering == null) {
            List<ResultadoClustering> resultados = clustering.ejecutar();
            paginaClustering = PaginasHtml.clustering(clustering, resultados);
        }
        return paginaClustering;
    }

    /** Lee ?clave=valor&otra=valor y decodifica %XX y +. */
    private static Map<String, String> parametros(URI uri) {
        Map<String, String> mapa = new HashMap<>();
        String consulta = uri.getRawQuery();
        if (consulta == null || consulta.isEmpty()) {
            return mapa;
        }
        for (String par : consulta.split("&")) {
            String[] partes = par.split("=", 2);
            String clave = URLDecoder.decode(partes[0], StandardCharsets.UTF_8);
            String valor = partes.length > 1 ? URLDecoder.decode(partes[1], StandardCharsets.UTF_8) : "";
            mapa.put(clave, valor);
        }
        return mapa;
    }

    private static String leerRecurso(String nombre) throws IOException {
        try (InputStream entrada = ServidorWeb.class.getResourceAsStream(nombre)) {
            if (entrada == null) {
                throw new IOException("No se encontró el recurso " + nombre);
            }
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void enviar(HttpExchange intercambio, int estado, String tipo, String contenido) throws IOException {
        byte[] bytes = contenido.getBytes(StandardCharsets.UTF_8);
        intercambio.getResponseHeaders().set("Content-Type", tipo);
        intercambio.sendResponseHeaders(estado, bytes.length);
        try (OutputStream salida = intercambio.getResponseBody()) {
            salida.write(bytes);
        }
    }
}