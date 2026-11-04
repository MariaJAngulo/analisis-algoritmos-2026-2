# Análisis de algoritmos en el contexto de la bibliometría

Requerimiento 3: ranking de términos con Counting Sort, Radix Sort y Bucket Sort
sobre los abstracts de los 20 artículos (`datos/articulos.json`).

## Cómo ejecutar

En IntelliJ: abrir `src/main/java/org/example/Main.java` y pulsar el botón verde junto a `main`.
La carpeta de trabajo debe ser la raíz del proyecto (donde están `datos/` y `pom.xml`).

Con Maven:

```
mvn compile exec:java -Dexec.mainClass=org.example.Main
```

La ejecución completa tarda alrededor de 30 segundos (la prueba de escalabilidad
incluye Bucket Sort con 10 cubetas sobre 100.000 datos, que es cuadrático).

## Clases

| Clase | Qué hace |
|---|---|
| `Main` | Orquesta todo, imprime resultados y genera los CSV |
| `Preprocesador` | Limpieza del texto, stopwords y conteo de frecuencias |
| `CountingSort` | Counting Sort descendente y estable, O(n + k) |
| `RadixSort` | Radix Sort LSD base 10, descendente y estable, O(d(n + b)) |
| `BucketSort` | Bucket Sort con 10 cubetas o una por frecuencia |
| `ExtractorPdf` | Extrae título, autores y abstract de los PDF y genera el JSON del corpus |
| `Medidor` | Calentamiento, repeticiones y estadísticas de tiempo (promedio, mediana, mín., máx., desv.) |
| `Escalabilidad` | Listas de 1.000 a 1.000.000 frecuencias para medir crecimiento |
| `ResultadoOrden` | Arreglos palabras/frecuencias y comparación de resultados |

## Resultados

Se guardan en `resultados/`:

- `top15.csv`: top 15 de cada algoritmo
- `tiempos_corpus.csv`: promedio, mínimo, máximo y desviación (ms)
- `cubetas_10.csv`: palabras por cubeta en Bucket Sort con 10 cubetas
- `escalabilidad.csv`: mediana, promedio, desviación y ns por elemento para cada n

## Extracción de los abstracts (PDF -> JSON)

`datos/articulos.json` se generó a partir de los 20 PDF con `org.example.modelo.ExtractorPdf`
(librería Apache PDFBox). Para reproducirlo:

1. Copiar los PDF en `datos/pdfs/` (el nombre de cada archivo empieza por su número).
2. Ejecutar desde la raíz del proyecto:

```
mvn compile exec:java -Dexec.mainClass=org.example.modelo.ExtractorPdf
```

Por cada PDF se leen las 2 primeras páginas, se unen las palabras cortadas con guion,
el abstract empieza después de la palabra "Abstract" (o "A B S T R A C T") y termina en
"Keywords", "Index Terms", "Introduction" o el símbolo ©. El título y los autores se toman
de los metadatos del PDF. La salida queda en `datos/articulos_extraidos.json`
(no sobrescribe `datos/articulos.json`).

## Escalabilidad y recolector de basura

Con el recolector por defecto de Java (G1), los arreglos de 1.000.000 de elementos (4 MB)
son "objetos humongous" y cada ejecución provoca pausas del GC, lo que infla el tiempo en
n = 1.000.000. Para comprobar que el crecimiento del algoritmo es lineal, se puede repetir
la prueba cambiando solo el recolector:

```
java -XX:+UseParallelGC -cp target/classes:<dependencias> org.example.seguimiento.MainSeguimiento
```

y para ver las pausas del GC: `-Xlog:gc` (aparecen como "G1 Humongous Allocation").
