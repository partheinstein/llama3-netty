# Intro

The main purpose of this project is for me to learn about multi-AI agent.

Agent Llamaty is llama3 inference built on top of Netty I/O server framework. Why Netty? Simply because I know it. :) There are some other reasons that supports the need for Netty's extensibility and performance. Disclaimer: I am well aware there a gazillions of options in terms for server frameworks but again this is a project designed to help me learn the parts I am interested.

## Interface

Interms of an interface, OpenAI's OpenAPI chat [spec](https://platform.openai.com/docs/api-reference/chat) is an option and there are some implementations out there like [vLLM](https://docs.vllm.ai/en/latest/getting_started/quickstart.html). However inter-op isn't the goal here, there are other options to consider. For example streaming RPC protocols like [gRPC](https://grpc.io/docs/what-is-grpc/core-concepts/) can enable agent-agent communication with less overhead, where each agent runs its own LLM to server inference requests from the other. This communication can perhaps even be asynchronous simulating human behaviour. It may even be a streaming protocol where an agent may be responsible for executing a plan from another agent but have to go back and forth to understand the context.

## Chat req/resp model

OpenAI's OpenAPI chat request and reponse model is a good starting point. Its request model supports plain text messages and images as input, as well as an extension to support "tools" to accuracy and to perhaps prevent hallucination.

In an agent-agent communication, do we need text messages? Not necessarily perhaps one agent runs a model trained on images while another is trained on source code; for instance an agent can analyze my sentiments through multi-model sentiment analysis [ref](https://web.stanford.edu/class/archive/cs/cs224n/cs224n.1244/final-projects/MubarakAliSeyedIbrahimPratyushMuthukumar.pdf) and instructs another agent to get a order a maple latte from a local coffee shop by invoking the shop's REST API or a book a vacation for me. The image inference may happen on one compute (e.g., on my phone), while the source code generation may happen on another (on my hosted server). In this case both agents use the same tokenizer to create the request and response embeddings, which may reduce the size of messages and compute overhead.

This is a learning exercise after all. So I will just adopt the OpenAPI chat req and resp model but implement it over gRPC.