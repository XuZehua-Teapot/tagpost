package com.google.tagpost;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import com.google.common.flogger.FluentLogger;

/** A dummy server that only manages startup/shutdown. */
public class TagpostServer {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private Server server;

  /** Main launches the server from the command line. */
  public static void main(String[] args) throws IOException, InterruptedException {
    final TagpostServer server = new TagpostServer();
    server.start();
    server.blockUntilShutdown();
  }

  private void start() throws IOException {
    /* The port on which the server should run */
    int port = 50053;
    server = ServerBuilder.forPort(port).addService(new TagpostService()).build().start();
    logger.atInfo().log("Server started, listening on " + port);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                logger.atInfo().log("*** shutting down gRPC server since JVM is shutting down");
                try {
                  TagpostServer.this.stop();
                } catch (InterruptedException e) {
                  e.printStackTrace(System.err);
                }
                logger.atInfo().log("*** server shut down");
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
}
