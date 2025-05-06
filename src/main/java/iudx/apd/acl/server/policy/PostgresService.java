package iudx.apd.acl.server.policy;

import static iudx.apd.acl.server.policy.util.Constants.DB_RECONNECT_ATTEMPTS;
import static iudx.apd.acl.server.policy.util.Constants.DB_RECONNECT_INTERVAL_MS;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import java.util.Map;

public class PostgresService {
  private final PgPool pool;
  private final String databaseSchema;

  public PostgresService(JsonObject config, Vertx vertx) {
    /* Database Properties */
    String databaseIp = config.getString("databaseIP");
    int databasePort = config.getInteger("databasePort");
    databaseSchema = config.getString("databaseSchema");
    String databaseName = config.getString("databaseName");
    String databaseUserName = config.getString("databaseUserName");
    String databasePassword = config.getString("databasePassword");
    int poolSize = config.getInteger("poolSize");
    Map<String, String> schemaProp = Map.of("search_path", databaseSchema);

    /* Set Connection Object and schema */
    PgConnectOptions connectOptions =
        new PgConnectOptions()
            .setPort(databasePort)
            .setHost(databaseIp)
            .setProperties(schemaProp)
            .setDatabase(databaseName)
            .setUser(databaseUserName)
            .setPassword(databasePassword)
            .setReconnectAttempts(DB_RECONNECT_ATTEMPTS)
            .setReconnectInterval(DB_RECONNECT_INTERVAL_MS);

    /* Pool options */
    PoolOptions poolOptions = new PoolOptions().setMaxSize(poolSize);

    /* Create the client pool */
    this.pool = PgPool.pool(vertx, connectOptions, poolOptions);
  }

  public PgPool getPool() {
    return pool;
  }
  public String getSchemaName(){
    return this.databaseSchema;
  }
}
