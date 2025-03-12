import time
from uuid import uuid4
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain.embeddings.openai import OpenAIEmbeddings
from qdrant_client import QdrantClient
from qdrant_client.models import VectorParams, Distance, PointStruct
import os

# Configuration
TXT_FILE_PATH = "ogrenciisleriv2.txt"
COLLECTION_NAME = "ogrenciisleri-500-100-COSINE-CENGDAHIL-somechanges"
EMBEDDING_SIZE = 1536  # Embedding size for text-embedding-ada-002
DISTANCE_METRIC = Distance.COSINE  # Use cosine similarity
OPENAI_API_KEY = "**"  # Replace with your OpenAI API key
BATCH_SIZE = 500  # Batch size for Qdrant upsertion

# Set OpenAI API key
os.environ["OPENAI_API_KEY"] = OPENAI_API_KEY

# Initialize Qdrant client
client = QdrantClient(host="localhost", port=6333)

# Function to load text file
def load_text_file(file_path):
    with open(file_path, "r", encoding="utf-8") as file:
        return file.read()

# Function to split text into chunks
def split_text_into_chunks(text, chunk_size=500, chunk_overlap=100):
    text_splitter = RecursiveCharacterTextSplitter(
        chunk_size=chunk_size,
        chunk_overlap=chunk_overlap
    )
    return text_splitter.create_documents([text])

# Function to ensure collection exists
def ensure_collection_exists(client, collection_name, embedding_size, distance_metric):
    collections = client.get_collections().collections
    collection_names = [collection.name for collection in collections]

    if collection_name not in collection_names:
        print(f"Collection '{collection_name}' does not exist. Creating...")
        client.create_collection(
            collection_name=collection_name,
            vectors_config=VectorParams(size=embedding_size, distance=distance_metric)
        )
        print(f"Collection '{collection_name}' created successfully.")
    else:
        print(f"Collection '{collection_name}' already exists. Skipping creation.")

# Function to upsert chunks into Qdrant with batching
def upsert_chunks_to_qdrant(chunks, client, collection_name, embedding, batch_size=500):
    print("Embedding text chunks and inserting into Qdrant in batches...")
    start_time = time.time()
    chunked_metadata = []

    for idx, item in enumerate(chunks):
        id = str(uuid4())
        content = item.page_content

        # Generate embeddings
        embed_start = time.time()
        content_vector = embedding.embed_documents([content])[0]
        chunked_metadata.append(PointStruct(id=id, vector=content_vector, payload={"page_content": content}))

        # Print progress for each chunk
        print(f"Chunk {idx + 1}/{len(chunks)} embedded in {time.time() - embed_start:.2f} seconds.")

        # Batch insertion
        if len(chunked_metadata) >= batch_size or idx == len(chunks) - 1:
            print(f"Inserting batch of {len(chunked_metadata)} points into Qdrant...")
            insert_start = time.time()
            client.upsert(collection_name=collection_name, wait=True, points=chunked_metadata)
            print(f"Batch inserted in {time.time() - insert_start:.2f} seconds.\n")
            chunked_metadata = []  # Clear batch

    print(f"All chunks inserted into Qdrant. Total time: {time.time() - start_time:.2f} seconds.\n")

# Main function
def main():
    # Load text data
    print("Loading text data...")
    text_data = load_text_file(TXT_FILE_PATH)

    # Split text into chunks
    print("Splitting text into chunks...")
    chunks = split_text_into_chunks(text_data)

    # Ensure the collection exists
    ensure_collection_exists(client, COLLECTION_NAME, EMBEDDING_SIZE, DISTANCE_METRIC)

    # Initialize embeddings
    print("Initializing embeddings...")
    embedding = OpenAIEmbeddings()

    # Insert data into Qdrant
    print("Inserting data into Qdrant...")
    upsert_chunks_to_qdrant(chunks, client, COLLECTION_NAME, embedding, batch_size=BATCH_SIZE)

    print("Data storage completed successfully.")

if __name__ == "__main__":
    main()
