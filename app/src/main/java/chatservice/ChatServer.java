/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package chatservice;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.protobuf.services.ProtoReflectionService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/** Server that manages startup/shutdown of a {@code Greeter} server. */
public class ChatServer {

  public final class ChatSvc extends ChatServiceGrpc.ChatServiceImplBase {
    @Override
    public void chat(
        Chat.Request request, io.grpc.stub.StreamObserver<Chat.Response> responseObserver) {
      try {

        String prompt = request.getMsg();
        String systemPrompt =
            "GENERAL INSTRUCTIONS\nYou are a domain expert. Your task is to break down a complex question into simpler sub-parts.\nUSER QUESTION{{user_question}}\nANSWER FORMAT\n{\"sub-questions\":[\"<FILL>\"]}";
        float temperature = 0.1f;
        float topp = 0.95f;
        long seed = System.nanoTime();
        Path modelPath =
            Paths.get("/home/partheinstein/git/llama3.java/Meta-Llama-3-8B-Instruct-Q4_0.gguf");
        int maxTokens = 512;
        boolean interactive = false;
        boolean stream = true;
        boolean echo = false;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Options options =
            new Options(
                modelPath,
                prompt,
                systemPrompt,
                interactive,
                temperature,
                topp,
                seed,
                maxTokens,
                stream,
                echo,
                out);

        Llama model = ModelLoader.loadModel(options.modelPath(), options.maxTokens());
        Sampler sampler =
            Llama3.selectSampler(
                model.configuration().vocabularySize,
                options.temperature(),
                options.topp(),
                options.seed());

        Llama3.runInstructOnce(model, sampler, options);
        String respMsg = new String(options.out().toByteArray(), StandardCharsets.UTF_8);
        Chat.Response resp = Chat.Response.newBuilder().setMsg(respMsg).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private static final Logger logger = Logger.getLogger(ChatServer.class.getName());

  private Server server;

  private void start() throws IOException {
    /* The port on which the server should run */
    int port = 50051;
    server =
        Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
            .addService(new ChatSvc())
            .addService(ProtoReflectionService.newInstance())
            .build()
            .start();
    logger.info("Server started, listening on " + port);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                  ChatServer.this.stop();
                } catch (InterruptedException e) {
                  e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
              }
            });
  }

  private void stop() throws InterruptedException {
    if (server != null) {
      server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }
  }

  /** Await termination on the main thread since the grpc library uses daemon threads. */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  /** Main launches the server from the command line. */
  public static void main(String[] args) throws IOException, InterruptedException {
    final ChatServer server = new ChatServer();
    server.start();
    server.blockUntilShutdown();
  }
}
