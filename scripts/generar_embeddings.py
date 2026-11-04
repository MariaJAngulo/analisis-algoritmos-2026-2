"""
Genera los vectores de los dos modelos de IA del Requerimiento 1.
Se ejecuta UNA sola vez, fuera de la aplicación Java (que solo lee los archivos resultantes).

Uso (desde la raíz del proyecto, donde está la carpeta datos/):
    pip install sentence-transformers
    python scripts/generar_embeddings.py              # genera los dos archivos
    python scripts/generar_embeddings.py glove        # solo GloVe
    python scripts/generar_embeddings.py minilm       # solo MiniLM

Salidas:
    datos/glove_corpus.txt       -> GloVe: una línea por palabra del corpus
    datos/embeddings_minilm.json -> MiniLM: { "id": [384 números] }

Modelo A: GloVe glove.6B.100d (Stanford NLP, Pennington et al. 2014).
          100 dimensiones, embeddings de PALABRAS estáticos.
          Descárgalo una vez (~822 MB) desde https://nlp.stanford.edu/data/glove.6B.zip
          y deja glove.6B.zip en la raíz del proyecto.
Modelo B: Sentence-BERT all-MiniLM-L6-v2 (Reimers y Gurevych, 2019).
          384 dimensiones, embeddings de FRASE contextuales.
          Descarga automática desde Hugging Face con la librería sentence-transformers.
"""
import json
import os
import re
import sys
import zipfile

RUTA_JSON = "datos/articulos.json"
ZIP_GLOVE = "glove.6B.zip"
URL_GLOVE = "https://nlp.stanford.edu/data/glove.6B.zip"
SALIDA_GLOVE = "datos/glove_corpus.txt"
SALIDA_MINILM = "datos/embeddings_minilm.json"


def cargar_articulos():
    with open(RUTA_JSON, encoding="utf-8") as f:
        return json.load(f)["papers"]


def palabras_del_corpus(articulos):
    """Todas las palabras (solo letras, minúsculas) que aparecen en los abstracts."""
    vocabulario = set()
    for a in articulos:
        texto = re.sub(r"[^a-z ]", " ", a["Abstract"].lower())
        vocabulario.update(texto.split())
    return vocabulario


def generar_glove(articulos):
    vocabulario = palabras_del_corpus(articulos)
    print(f"[GloVe] palabras distintas en los abstracts: {len(vocabulario)}")

    if not os.path.exists(ZIP_GLOVE):
        sys.exit("Falta glove.6B.zip. Descárgalo con el navegador desde\n  " + URL_GLOVE
                 + "\ny déjalo en la raíz del proyecto.")

    encontradas = 0
    with zipfile.ZipFile(ZIP_GLOVE) as z, \
            z.open("glove.6B.100d.txt") as entrada, \
            open(SALIDA_GLOVE, "w", encoding="utf-8") as salida:
        for linea in entrada:
            texto = linea.decode("utf-8")
            palabra = texto.split(" ", 1)[0]
            if palabra in vocabulario:  # solo nos quedamos con las palabras del corpus
                salida.write(texto if texto.endswith("\n") else texto + "\n")
                encontradas += 1
    print(f"[GloVe] {encontradas} palabras guardadas en {SALIDA_GLOVE} "
          f"({len(vocabulario) - encontradas} no existen en GloVe y Java las ignorará)")


def generar_minilm(articulos):
    from sentence_transformers import SentenceTransformer  # import aquí: solo se pide si se usa

    modelo = SentenceTransformer("sentence-transformers/all-MiniLM-L6-v2")
    # Ojo: el modelo corta cada texto a 256 piezas de palabra (word pieces);
    # los abstracts más largos se truncan. Hay que mencionarlo en el documento.
    vectores = modelo.encode([a["Abstract"] for a in articulos])
    resultado = {a["id"]: [round(float(x), 6) for x in v] for a, v in zip(articulos, vectores)}
    with open(SALIDA_MINILM, "w", encoding="utf-8") as f:
        json.dump(resultado, f)
    print(f"[MiniLM] {len(resultado)} vectores de {len(vectores[0])} dimensiones "
          f"guardados en {SALIDA_MINILM}")


if __name__ == "__main__":
    quien = sys.argv[1] if len(sys.argv) > 1 else "todos"
    articulos = cargar_articulos()
    if quien in ("todos", "glove"):
        generar_glove(articulos)
    if quien in ("todos", "minilm"):
        generar_minilm(articulos)