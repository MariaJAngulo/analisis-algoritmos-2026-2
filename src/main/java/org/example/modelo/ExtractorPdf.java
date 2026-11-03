package org.example.modelo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extrae el título, los autores y el abstract de los 20 PDF del corpus
 * y los guarda en un archivo JSON con el mismo formato que lee Corpus.
 *
 * Uso: java org.example.modelo.ExtractorPdf [carpetaPdf] [archivoSalida]
 * Por defecto lee datos/pdfs y escribe datos/articulos_extraidos.json
 * (no sobrescribe datos/articulos.json).
 *
 * Pasos para cada PDF:
 * 1. PDFBox (PDFTextStripper) obtiene el texto de las 2 primeras páginas,
 *    que es donde está el abstract.
 * 2. Se unen las palabras cortadas con guion al final de una línea.
 * 3. Inicio del abstract: la primera aparición de "Abstract"
 *    (también "A B S T R A C T", como lo escribe Elsevier).
 * 4. Fin del abstract: lo primero que aparezca después entre
 *    "Keywords", "Key words" o "Index Terms" al inicio de una línea,
 *    el título "Introduction" o el símbolo de copyright.
 * 5. Se reemplazan los saltos de línea por espacios.
 * 6. Título y autores salen de los metadatos del PDF; si el título no
 *    está, se usa el nombre del archivo sin el número inicial.
 *
 * Al final, si existe datos/articulos.json, compara cada abstract
 * extraído con el guardado (coeficiente de Jaccard sobre las palabras)
 * para saber qué artículos hay que revisar a mano.
 */
public class ExtractorPdf {

    private static final int PAGINAS_A_LEER = 2;
    private static final int LARGO_MAXIMO = 3000; // si no se encuentra el final

    private static final Pattern INICIO = Pattern.compile(
            "(?i)a\\s?b\\s?s\\s?t\\s?r\\s?a\\s?c\\s?t[\\s:.\\-—]*");

    private static final Pattern FIN = Pattern.compile(
            "(?i)(\\n\\s*(key\\s?words|index terms)|\\n\\s*(1\\.?\\s*)?introduction\\b|©|\\bcopyright\\b)");

    public static void main(String[] args) throws IOException {
        File carpeta = new File(args.length > 0 ? args[0] : "datos/pdfs");
        File salida = new File(args.length > 1 ? args[1] : "datos/articulos_extraidos.json");

        File[] pdfs = carpeta.listFiles((dir, nombre) -> nombre.toLowerCase().endsWith(".pdf"));
        if (pdfs == null || pdfs.length == 0) {
            System.out.println("No hay PDF en " + carpeta.getAbsolutePath());
            return;
        }
        // Ordenar por el número con el que empieza el nombre ("1. ...", "2. ...")
        Arrays.sort(pdfs, (a, b) -> Integer.compare(numeroInicial(a.getName()), numeroInicial(b.getName())));

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode raiz = mapper.createObjectNode();
        ArrayNode papers = raiz.putArray("papers");

        for (int i = 0; i < pdfs.length; i++) {
            File pdf = pdfs[i];
            try (PDDocument documento = Loader.loadPDF(pdf)) {

                PDFTextStripper extractor = new PDFTextStripper();
                extractor.setStartPage(1);
                extractor.setEndPage(Math.min(PAGINAS_A_LEER, documento.getNumberOfPages()));
                String texto = extractor.getText(documento);

                ObjectNode articulo = papers.addObject();
                articulo.put("id", String.valueOf(i + 1));
                articulo.put("title", titulo(documento.getDocumentInformation(), pdf.getName()));
                ArrayNode autores = articulo.putArray("authors");
                for (String autor : autores(documento.getDocumentInformation())) {
                    autores.add(autor);
                }
                articulo.put("Abstract", extraerAbstract(texto));
                articulo.put("sourceFile", pdf.getName());

                if (autores.isEmpty()) {
                    System.out.println("Aviso: " + pdf.getName() + " no tiene autores en los metadatos");
                }
            }
        }

        mapper.writerWithDefaultPrettyPrinter().writeValue(salida, raiz);
        System.out.println("Extraídos " + papers.size() + " artículos en " + salida.getPath());

        File guardado = new File("datos/articulos.json");
        if (guardado.exists() && !guardado.equals(salida)) {
            compararCon(mapper.readTree(guardado).get("papers"), papers);
        }
    }

    /** Recorta el abstract del texto de las primeras páginas. */
    static String extraerAbstract(String texto) {
        // Une palabras cortadas al final de la línea: "algo-\nritmo" -> "algoritmo"
        texto = texto.replaceAll("(\\p{L})-\\s*\\r?\\n\\s*(\\p{L})", "$1$2");

        Matcher inicio = INICIO.matcher(texto);
        if (!inicio.find()) {
            return "";
        }
        int desde = inicio.end();

        Matcher fin = FIN.matcher(texto);
        int hasta = fin.find(desde) ? fin.start() : Math.min(texto.length(), desde + LARGO_MAXIMO);

        return texto.substring(desde, hasta).replaceAll("\\s+", " ").trim();
    }

    private static String titulo(PDDocumentInformation info, String archivo) {
        String titulo = info.getTitle();
        if (titulo != null && !titulo.isBlank()) {
            return titulo.trim();
        }
        // "3. Myths mis and ... literature.pdf" -> "Myths mis and ... literature"
        return archivo.replaceFirst("^\\d+\\.\\s*", "").replaceFirst("(?i)\\.pdf$", "").trim();
    }

    private static String[] autores(PDDocumentInformation info) {
        String autor = info.getAuthor();
        if (autor == null || autor.isBlank()) {
            return new String[0];
        }
        return autor.trim().split("\\s*(;|,| and )\\s*");
    }

    private static int numeroInicial(String nombre) {
        Matcher m = Pattern.compile("^(\\d+)").matcher(nombre);
        return m.find() ? Integer.parseInt(m.group(1)) : Integer.MAX_VALUE;
    }

    /** Compara los abstracts extraídos con los de datos/articulos.json. */
    private static void compararCon(JsonNode guardados, ArrayNode extraidos) {
        Map<String, String> porArchivo = new HashMap<>();
        for (JsonNode a : guardados) {
            porArchivo.put(a.get("sourceFile").asText(), a.get("Abstract").asText());
        }
        System.out.println("\n--- COMPARACIÓN CON datos/articulos.json (Jaccard de palabras) ---");
        for (JsonNode a : extraidos) {
            String archivo = a.get("sourceFile").asText();
            String anterior = porArchivo.get(archivo);
            if (anterior == null) {
                System.out.println(archivo + ": no está en articulos.json");
                continue;
            }
            double j = jaccard(a.get("Abstract").asText(), anterior);
            System.out.printf("%5.2f  %s%s%n", j, archivo, j < 0.9 ? "   <- revisar" : "");
        }
    }

    private static double jaccard(String a, String b) {
        Set<String> x = palabras(a);
        Set<String> y = palabras(b);
        if (x.isEmpty() && y.isEmpty()) {
            return 1.0;
        }
        Set<String> union = new HashSet<>(x);
        union.addAll(y);
        x.retainAll(y);
        return (double) x.size() / union.size();
    }

    private static Set<String> palabras(String texto) {
        Set<String> conjunto = new HashSet<>();
        for (String p : texto.toLowerCase().split("[^a-z]+")) {
            if (!p.isEmpty()) {
                conjunto.add(p);
            }
        }
        return conjunto;
    }
}
