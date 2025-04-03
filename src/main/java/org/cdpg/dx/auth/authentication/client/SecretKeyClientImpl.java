package org.cdpg.dx.auth.authentication.client;
import io.vertx.core.Future;
import io.vertx.ext.web.client.WebClient;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.util.ConfigKeys;


public class SecretKeyClientImpl implements SecretKeyClient {
    private static final Logger LOGGER = LogManager.getLogger(SecretKeyClientImpl.class);

    private final WebClient webClient;
    private final String authHost;
    private final String authCertPath;

    public SecretKeyClientImpl(WebClient webClient, JsonObject config) {
        this.webClient = webClient; // Use WebClient from MainVerticle
        this.authHost = config.getString(ConfigKeys.AUTH_HOST);
        this.authCertPath = config.getString(ConfigKeys.DX_AUTH_BASE_PATH) + "/.well-known/jwks.json";
    }

    @Override
    public Future<JsonObject> fetchSecretKey() {
        return webClient.get(443, authHost, authCertPath)
                .send()
                .map(response -> response.bodyAsJsonObject().getJsonArray("keys").getJsonObject(0)) // Extract key
                .recover(error -> {
                    LOGGER.error("Failed to get JWT public key: {}", error.getMessage());
                    return Future.failedFuture(error);
                });
    }
}
