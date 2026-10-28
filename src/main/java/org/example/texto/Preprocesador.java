package org.example.texto;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.seguimiento.ResultadoOrden;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Convierte los abstracts de los artículos en una lista de
 * palabras distintas con su frecuencia.
 *
 * Pasos: minúsculas, quitar lo que no sea letra, separar por
 * espacios, quitar palabras vacías (stopwords) y palabras de
 * menos de 2 letras, y contar.
 */
public class Preprocesador {

    public static final int LONGITUD_MINIMA = 2;

    private static final String[] PALABRAS_VACIAS = {
            "the", "and", "for", "are", "but", "not", "you",
            "all", "any", "can", "had", "her", "was", "one",
            "our", "out", "day", "get", "has", "him", "his",
            "how", "its", "may", "new", "now", "old", "see",
            "two", "way", "who", "boy", "did", "let", "put",
            "say", "she", "too", "use", "with", "this", "that",
            "from", "they", "have", "been", "were", "will",
            "would", "there", "their", "what", "about", "which",
            "when", "where", "while", "into", "than", "then",
            "them", "these", "those", "also", "such", "more",
            "most", "some", "only", "other", "each", "many",
            "much", "very", "just", "over", "under", "between",
            "through", "during", "after", "before", "because",
            "being", "does", "doing", "having", "your",
            "ours", "themselves", "theirs", "itself", "it",
            "a", "an", "as", "at", "by", "if", "in",
            "is", "of", "on", "or", "to", "up", "we", "he",
            "i", "me", "my", "no", "so", "do", "be", "am",
            "whom", "why", "own", "same",
            // Números romanos y prefijos que quedan al quitar guiones
            "ii", "iv", "co"
    };

    private final Set<String> palabrasVacias = new HashSet<>();

    // Datos del último corpus procesado
    private int totalPalabrasProcesadas;
    private int totalPalabrasConservadas;

    public Preprocesador() {
        for (String palabra : PALABRAS_VACIAS) {
            palabrasVacias.add(palabra);
        }
    }

    public int cantidadPalabrasVacias() {
        return palabrasVacias.size();
    }

    public int getTotalPalabrasProcesadas() {
        return totalPalabrasProcesadas;
    }

    public int getTotalPalabrasConservadas() {
        return totalPalabrasConservadas;
    }

    /**
     * Pasos: minúsculas, quitar lo que no sea letra, separar por espacios,
     * quitar stopwords y palabras de menos de 2 letras. Costo O(L), L = largo del texto.
     */
    public List<String> tokenizar(String texto) {
        List<String> tokens = new ArrayList<>();
        for (String palabra : palabrasCrudas(texto)) {
            if (palabra.length() >= LONGITUD_MINIMA && !palabrasVacias.contains(palabra)) {
                tokens.add(palabra);
            }
        }
        return tokens;
    }

    /**
     * Solo minúsculas y espacios simples. Conserva puntuación y dígitos: es el texto
     * que usa la búsqueda de patrones, que debe respetar el original. O(L).
     */
    public String normalizar(String texto) {
        return texto.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    // Minúsculas + solo letras + separar por espacios (sin filtrar stopwords)
    private List<String> palabrasCrudas(String texto) {
        String limpio = texto.toLowerCase().replaceAll("[^a-z ]", " ").trim();
        List<String> palabras = new ArrayList<>();
        for (String palabra : limpio.split("\\s+")) {
            if (!palabra.isEmpty()) {
                palabras.add(palabra);
            }
        }
        return palabras;
    }

    /**
     * Cuenta las palabras de los abstracts (entrega de seguimiento).
     * Devuelve las palabras en orden alfabético para que los
     * empates queden en el mismo orden en los tres algoritmos
     * (todos son estables).
     */
    public ResultadoOrden contarFrecuencias(JsonNode articulos) {

        Map<String, Integer> frecuencias = new HashMap<>();
        totalPalabrasProcesadas = 0;
        totalPalabrasConservadas = 0;

        for (int i = 0; i < articulos.size(); i++) {
            String resumen = articulos.get(i).get("Abstract").asText();

            totalPalabrasProcesadas += palabrasCrudas(resumen).size();
            for (String palabra : tokenizar(resumen)) {
                frecuencias.put(palabra, frecuencias.getOrDefault(palabra, 0) + 1);
                totalPalabrasConservadas++;
            }
        }

        // TreeMap: recorre las claves en orden alfabético
        TreeMap<String, Integer> ordenAlfabetico = new TreeMap<>(frecuencias);

        String[] palabrasUnicas = new String[ordenAlfabetico.size()];
        int[] cantidades = new int[ordenAlfabetico.size()];

        int indice = 0;
        for (Map.Entry<String, Integer> entrada : ordenAlfabetico.entrySet()) {
            palabrasUnicas[indice] = entrada.getKey();
            cantidades[indice] = entrada.getValue();
            indice++;
        }

        return new ResultadoOrden(palabrasUnicas, cantidades);
    }
}
