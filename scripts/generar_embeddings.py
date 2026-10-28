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
          Descarga: https://nlp.stanford.edu/data/glove.6B.zip o su espejo en Hugging Face
          (stanfordnlp/glove), ~822 MB, se hace una vez.
          Si ya tienes glove.6B.zip en la carpeta del proyecto, se usa ese archivo.
Modelo B: Sentence-BERT all-MiniLM-L6-v2 (Reimers y Gurevych, 2019).
          384 dimensiones, embeddings de FRASE contextuales.
          Descarga automática desde Hugging Face con la librería sentence-transformers.
"""
import json
import os
import re
import sys
import socket
import urllib.request
import zipfile

RUTA_JSON = "datos/articulos.json"
ZIP_GLOVE = "glove.6B.zip"
# Se prueban en orden; el segundo es el espejo oficial de Stanford en Hugging Face.
URLS_GLOVE = [
    "https://huggingface.co/stanfordnlp/glove/resolve/main/glove.6B.zip",
    "https://nlp.stanford.edu/data/glove.6B.zip",
]
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


def descargar_glove():
    """Prueba cada URL hasta lograr bajar el zip. Se guarda como .part y se renombra al final,
    así un corte de red no deja un zip incompleto que parezca válido."""
    socket.setdefaulttimeout(60)
    parcial = ZIP_GLOVE + ".part"
    for url in URLS_GLOVE:
        try:
            print(f"[GloVe] descargando {url} (~822 MB, solo la primera vez)...")
            urllib.request.urlretrieve(url, parcial)
            os.replace(parcial, ZIP_GLOVE)
            return
        except Exception as error:
            print(f"[GloVe] falló esa URL: {error}")
    if os.path.exists(parcial):
        os.remove(parcial)
    sys.exit("No se pudo descargar GloVe. Bájalo con el navegador desde alguna de estas URL "
             "y deja glove.6B.zip en la raíz del proyecto:\n  " + "\n  ".join(URLS_GLOVE))


def generar_glove(articulos):
    vocabulario = palabras_del_corpus(articulos)
    print(f"[GloVe] palabras distintas en los abstracts: {len(vocabulario)}")

    if not os.path.exists(ZIP_GLOVE):
        descargar_glove()

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