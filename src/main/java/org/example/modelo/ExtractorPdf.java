package org.example.modelo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
public class ExtractorPdf {

    private static final int PAGINAS_A_LEER = 2;
    private static final int LARGO_MAXIMO = 3000; // si no se encuentra el final
    private static final int MINIMO = 100;        // un abstract tiene al menos 100 caracteres
    private static final String[] PALABRAS_FIN = {"\nkeywords", "\nkey words", "\nindex terms", "introduction", "©"};

    public static void main(String[] args) throws IOException {
        File carpeta = new File("datos/pdfs");
        File salida = new File("datos/articulos_extraidos.json");

        File[] pdfs = carpeta.listFiles();
        if (pdfs == null || pdfs.length == 0) {
            System.out.println("No hay PDF en " + carpeta.getAbsolutePath());
            return;
        }
        // Ordenar por el número con que empieza el nombre: "1. ...", "2. ...", ..., "20. ..."
        Arrays.sort(pdfs, (a, b) -> numero(a.getName()) - numero(b.getName()));

        List<Map<String, Object>> papers = new ArrayList<>();
        for (File pdf : pdfs) {
            if (!pdf.getName().toLowerCase().endsWith(".pdf")) {
                continue;
            }
            try (PDDocument documento = Loader.loadPDF(pdf)) {
                PDFTextStripper lector = new PDFTextStripper();
                lector.setStartPage(1);
                lector.setEndPage(Math.min(PAGINAS_A_LEER, documento.getNumberOfPages()));
                String texto = lector.getText(documento);
                PDDocumentInformation info = documento.getDocumentInformation();

                Map<String, Object> articulo = new LinkedHashMap<>();
                articulo.put("id", String.valueOf(numero(pdf.getName())));
                articulo.put("title", titulo(info, pdf.getName()));
                articulo.put("authors", autores(info));
                articulo.put("Abstract", extraerAbstract(texto));
                articulo.put("sourceFile", pdf.getName());
                papers.add(articulo);
            }
        }

        Map<String, Object> raiz = new LinkedHashMap<>();
        raiz.put("papers", papers);
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(salida, raiz);
        System.out.println("Extraídos " + papers.size() + " artículos en " + salida.getPath());
    }

    /** Recorta el abstract del texto de las primeras páginas. */
    static String extraerAbstract(String texto) {
        texto = texto.replace("\r\n", "\n");   // en Windows los saltos de línea son \r\n
        // Une palabras cortadas al final de la línea: "algo-\nritmo" -> "algoritmo"
        texto = texto.replace("-\n", "");
        String minusculas = texto.toLowerCase();

        // Elsevier escribe el título con espacios: "A B S T R A C T". Se busca primero,
        // porque en esos artículos la palabra "abstract" también aparece en el texto.
        String marca = "a b s t r a c t";
        int inicio = minusculas.indexOf(marca);
        if (inicio == -1) {
            marca = "abstract";
            inicio = minusculas.indexOf(marca);
        }
        if (inicio == -1) {
            return "";
        }
        int desde = inicio + marca.length();

        // El fin es la primera palabra de PALABRAS_FIN que aparezca después del inicio
        int hasta = Math.min(texto.length(), desde + LARGO_MAXIMO);
        for (String palabra : PALABRAS_FIN) {
            int posicion = minusculas.indexOf(palabra, desde);
            // Se ignora si aparece pegada al título (en algunos PDF las palabras
            // clave quedan justo después de "Abstract" por las dos columnas)
            if (posicion > desde + MINIMO && posicion < hasta) {
                hasta = posicion;
            }
        }

        String resumen = texto.substring(desde, hasta).replaceAll("\\s+", " ").trim();
        // Quita los dos puntos o el guion que suelen ir después de "Abstract"
        if (resumen.startsWith(":") || resumen.startsWith("-") || resumen.startsWith(".")) {
            resumen = resumen.substring(1).trim();
        }
        // Quita el número del título "1. Introduction" si quedó al final
        if (resumen.endsWith(" 1.") || resumen.endsWith(" 1")) {
            resumen = resumen.substring(0, resumen.lastIndexOf(" 1")).trim();
        }
        return resumen;
    }

    /** "12. Titulo del articulo.pdf" -> 12 (si no empieza con número, va al final). */
    private static int numero(String archivo) {
        int punto = archivo.indexOf('.');
        try {
            return Integer.parseInt(archivo.substring(0, punto).trim());
        } catch (Exception e) {
            return 999;
        }
    }

    private static String titulo(PDDocumentInformation info, String archivo) {
        String titulo = info.getTitle();
        if (titulo != null && !titulo.isBlank()) {
            return titulo.trim();
        }
        // "3. Myths and preconceptions.pdf" -> "Myths and preconceptions"
        String sinExtension = archivo.substring(0, archivo.length() - 4);
        int punto = sinExtension.indexOf(". ");
        return punto == -1 ? sinExtension : sinExtension.substring(punto + 2).trim();
    }

    private static List<String> autores(PDDocumentInformation info) {
        List<String> lista = new ArrayList<>();
        String autor = info.getAuthor();
        if (autor == null || autor.isBlank()) {
            return lista;
        }
        for (String nombre : autor.split(";|,| and ")) {
            if (!nombre.isBlank()) {
                lista.add(nombre.trim());
            }
        }
        return lista;
    }
}
