package org.cdpg.dx.aaa.client;
import io.vertx.core.json.JsonObject;
import io.vertx.core.Future;

public interface AAAClient {
    Future<JsonObject> fetchUserData(String userId, String role, String resourceServer);
}