package com.example.starter;


import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> start) throws Exception {

    vertx.deployVerticle(new HelloVerticle());
    Router router = Router.router(vertx);
    router.get("/hello").handler(this::helloVertx);
    router.get("/hello/:name").handler(this::helloName);

    /*
    int httpPort;
    try{
      httpPort = Integer.parseInt(System.getProperty("http.port", "8080"));
    }catch (NumberFormatException nfe){
      httpPort = 8080;
    }
     */

    ConfigStoreOptions defaultConfig = new ConfigStoreOptions()
      .setType("file")
      .setFormat("json")
      .setConfig(new JsonObject().put("path","config.json"));

    ConfigRetrieverOptions opts = new ConfigRetrieverOptions()
      .addStore(defaultConfig);

    ConfigRetriever cfgRetreiever = ConfigRetriever.create(vertx, opts);

    Handler<AsyncResult<JsonObject>> handler = asyncResult -> this.handleConfigResults(start, asyncResult, router);
    cfgRetreiever.getConfig(handler);

  }

  void handleConfigResults(Promise<Void> start, AsyncResult<JsonObject> asyncResult, Router router){

      if (asyncResult.succeeded()){
        JsonObject config = asyncResult.result();
        JsonObject http = config.getJsonObject("http");
        int httpPort = http.getInteger("port");
        vertx.createHttpServer().requestHandler(router).listen(httpPort);
        start.complete();
      } else {
        start.fail("unable to load configuration");
      }
    }

  void helloVertx(RoutingContext ctx){
    vertx.eventBus().request("hello.vertx.addr", "", reply -> {
      ctx.request().response().end((String) reply.result().body());
    });
    //ctx.request().response().end("Hello Vertx World");
  }
  void helloName(RoutingContext ctx){
    String name = ctx.pathParam("name");
    vertx.eventBus().request("hello.name.addr", name, reply -> {
        ctx.request().response().end((String) reply.result().body());
      });
    //ctx.request().responsed(String.format("Hello %s!", name));
  }
}
