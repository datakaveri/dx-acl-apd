package org.cdpg.dx.auth.authentication.client;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * Client interface to fetch secret key from the authentication server.
 */
public interface SecretKeyClient {
    Future<JsonObject> fetchSecretKey();
}
