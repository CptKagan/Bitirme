import os
import sys
from qdrant_client import QdrantClient
from langchain.vectorstores import Qdrant
from langchain.embeddings.openai import OpenAIEmbeddings
from langchain.prompts import PromptTemplate
from langchain.chat_models import ChatOpenAI
from langchain.chains import RetrievalQA

def main():
    if len(sys.argv) < 2:
        print("Error: No question provided. Usage: python script.py '<question>'")
        sys.exit(1)

    question = sys.argv[1]  # Get the question from command-line arguments

    # OpenAI API key
    os.environ["OPENAI_API_KEY"] = "****"  # Replace with your OpenAI API key

    # Qdrant configuration
    collection_name = "ogrenciisleri-500-100-COSINE-CENGDAHIL-somechanges"
    client = QdrantClient(host="localhost", port=6333)

    # Check if the collection exists
    collections = client.get_collections().collections
    collection_names = [collection.name for collection in collections]
    if collection_name not in collection_names:
        raise ValueError(f"Collection '{collection_name}' does not exist in Qdrant.")

    # Initialize Qdrant vector store
    vector_store = Qdrant(
        client=client,
        collection_name=collection_name,
        embeddings=OpenAIEmbeddings()
    )

    # Create a retriever
    retriever = vector_store.as_retriever(search_kwargs={"k": 10})

    # Define the prompt template
    template = """
    Aşağıdaki bağlamda verilen bilgileri kullanarak soruyu eksiksiz bir şekilde yanıtla.
    Bağlamda yanıt yoksa, verilen bilgilerden en alakalı olanı yaz.
    Sorunun cevabını bağlamdaki cümlelerden aynen al.
    Verilen bağlam harici bir bilgi kullanma.

    Bağlam:
    {context}

    Soru:
    {question}

    Cevap:
    """
    prompt = PromptTemplate(template=template, input_variables=["context", "question"])

    # Initialize OpenAI Chat Model
    openai_client = ChatOpenAI(
        model="gpt-3.5-turbo",
        temperature=0.7,
        max_tokens=250
    )

    # Build the RetrievalQA chain
    qa_chain = RetrievalQA.from_chain_type(
        llm=openai_client,
        retriever=retriever,
        chain_type_kwargs={"prompt": prompt}
    )

    # Query the chain
    response = qa_chain.run(question)
    print("Response:", response)

if __name__ == "__main__":
    main()
