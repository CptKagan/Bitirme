import time
from uuid import uuid4
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain.chains import create_retrieval_chain
from langchain_qdrant import QdrantVectorStore
from langchain.prompts import ChatPromptTemplate
from langchain.chains.combine_documents import create_stuff_documents_chain
from qdrant_client import QdrantClient
from qdrant_client.models import Distance, VectorParams, PointStruct, NamedVector
from langchain_ollama import OllamaLLM, OllamaEmbeddings
import sys

# Model setup
MODEL = "llama3.1:8b"
model = OllamaLLM(model=MODEL)  # For generation
embeddings = OllamaEmbeddings(model=MODEL)  # For embeddings

# Load text from a file
def load_text_file(file_path):
    start_time = time.time()
    with open(file_path, "r", encoding="utf-8") as file:
        content = file.read()
    return content

# Chunk and process text
def chunk_text(text, chunk_size=500, chunk_overlap=100):
    start_time = time.time()
    text_splitter = RecursiveCharacterTextSplitter.from_tiktoken_encoder(
        model_name="gpt-4",
        chunk_size=chunk_size,
        chunk_overlap=chunk_overlap,
    )
    chunks = text_splitter.create_documents([text])
    return chunks

# Upsert chunks into Qdrant
def upsert_chunks_to_qdrant(chunks, client, collection_name, batch_size=500):
    start_time = time.time()
    chunked_metadata = []

    for idx, item in enumerate(chunks):
        id = str(uuid4())
        content = item.page_content

        # Generate embeddings
        embed_start = time.time()
        content_vector = embeddings.embed_documents([content])[0]
        chunked_metadata.append(PointStruct(id=id, vector={"content": content_vector}, payload={"page_content": content}))

        # Batch insertion
        if len(chunked_metadata) >= batch_size or idx == len(chunks) - 1:
            insert_start = time.time()
            client.upsert(collection_name=collection_name, wait=True, points=chunked_metadata)
            chunked_metadata = []

# Search Qdrant
def search_qdrant(client, collection_name, query_text, top_k=30):
    query_vector = embeddings.embed_documents([query_text])[0]
    start_time = time.time()
    search_results = client.search(
        collection_name=collection_name,
        query_vector=NamedVector(name="content", vector=query_vector),
        with_payload=["page_content", "metadata"],
        limit=top_k,
    )
    return search_results

# Main execution
def main():
    if len(sys.argv) < 2:
        print("Error: No question provided. Usage: python script.py '<question>'")
        sys.exit(1)

    query_text = sys.argv[1]  # Get the question from the command-line arguments
    txt_path = "C:\\Users\\mkaga\\OneDrive\\Masaüstü\\BITIRME\\LAMA3.1FIRSTRUN\\ogrenciisleri.txt"
    collection_name = "ogrenciisleri-500-100-COSINE-LLAMAICIN"
    client = QdrantClient("localhost", port=6333)

    # Load and chunk text
    text = load_text_file(txt_path)
    chunks = chunk_text(text)

    # Check and create collection
    collections = [collection.name for collection in client.get_collections().collections]
    if collection_name not in collections:
        client.create_collection(
            collection_name=collection_name,
            vectors_config={"content": VectorParams(size=4096, distance=Distance.DOT)},
        )
        upsert_chunks_to_qdrant(chunks, client, collection_name)

    # Perform search and RAG
    search_results = search_qdrant(client, collection_name, query_text)

    # Perform RAG
    vectorstore = QdrantVectorStore(
        client=client,
        collection_name=collection_name,
        embedding=embeddings,
        vector_name="content",
        distance=Distance.COSINE,
    )
    retriever = vectorstore.as_retriever(search_kwargs={"k": 20, "score_threshold": 0.8})

    template = """
    Sen bir bilgi sağlama görevinde uzmanlaşmış bir asistansın. \
    Soruyu yanıtlamak için yalnızca aşağıdaki bağlamdaki bilgileri kullan. \
    Bağlamda bulunmayan bilgileri kullanman yasak. \
    Yanıtlarında kişisel ifadeler veya tahminlerde bulunma. \
    Türkçe karakterleri doğru bir şekilde kullan.

    Soru: {input}
    Bağlam: {context}

    Cevap:
    """

    prompt = ChatPromptTemplate.from_template(template)
    combine_docs_chain = create_stuff_documents_chain(model, prompt)
    retrieval_chain = create_retrieval_chain(retriever, combine_docs_chain)

    result = retrieval_chain.invoke({"input": query_text})
    print(result['answer'])


if __name__ == "__main__":
    main()
