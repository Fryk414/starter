package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

import java.util.UUID;

public class HelloVerticle extends AbstractVerticle {

  String verticleId = UUID.randomUUID().toString();

  @Override
  public void start(Promise<Void> start) {
    vertx.eventBus().consumer("hello.vertx.addr", msg -> {
      msg.reply("hello vertx.world");
    });
    vertx.eventBus().consumer("hello.name.addr", msg -> {
      String name = (String) msg.body();
      msg.reply(String.format("hello %s from %s", name, verticleId));
    });
  }
}
