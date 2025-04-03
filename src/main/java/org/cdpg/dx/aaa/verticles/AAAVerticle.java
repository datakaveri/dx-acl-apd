package org.cdpg.dx.aaa.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.cdpg.dx.aaa.client.AAAClient;
import org.cdpg.dx.aaa.client.AAAWebClient;
import org.cdpg.dx.aaa.service.AAAService;
import org.cdpg.dx.aaa.service.AAAServiceImpl;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class AAAVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        JsonObject config = config();  // Load config from Vertx context
        WebClientOptions webClientOptions = new WebClientOptions();
        webClientOptions.setTrustAll(false).setVerifyHost(true).setSsl(true);
        WebClient webClient = WebClient.create(vertx, webClientOptions);

        AAAClient aaaClient = new AAAWebClient(config, webClient); // Create AAAWebClient
        AAAService aaaService = new AAAServiceImpl(aaaClient);  // Inject client into service

        new ServiceBinder(vertx)
                .setAddress("service.aaa")
                .register(AAAService.class, aaaService);  // Register as service proxy

        startPromise.complete();
    }
}