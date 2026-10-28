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
| `Medidor` | Calentamiento, repeticiones y estadísticas de tiempo |
| `Escalabilidad` | Listas de 1.000 a 1.000.000 frecuencias para medir crecimiento |
| `ResultadoOrden` | Arreglos palabras/frecuencias y comparación de resultados |

## Resultados

Se guardan en `resultados/`:

- `top15.csv`: top 15 de cada algoritmo
- `tiempos_corpus.csv`: promedio, mínimo, máximo y desviación (ms)
- `cubetas_10.csv`: palabras por cubeta en Bucket Sort con 10 cubetas
- `escalabilidad.csv`: tiempo promedio vs n
