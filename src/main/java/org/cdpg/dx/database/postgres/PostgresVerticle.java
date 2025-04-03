package org.cdpg.dx.database.postgres;

import static iudx.apd.acl.server.policy.util.Constants.DB_RECONNECT_ATTEMPTS;
import static iudx.apd.acl.server.policy.util.Constants.DB_RECONNECT_INTERVAL_MS;
import static org.cdpg.dx.acl.policy.common.Constants.PG_SERVICE_ADDRESS;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.serviceproxy.ServiceBinder;
import java.util.Map;
import org.cdpg.dx.database.postgres.service.PostgresService;
import org.cdpg.dx.database.postgres.service.PostgresServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PostgresVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(PostgresService.class);

    private MessageConsumer<JsonObject> consumer;
    private ServiceBinder binder;

  @Override
    public void start() throws Exception {
    LOGGER.info("hereeeeeee");
      String databaseIp = config().getString("databaseIp");
      int databasePort = config().getInteger("databasePort");
      String databaseName = config().getString("databaseName");
      String databaseSchema = config().getString("databaseSchema");
      String databaseUserName = config().getString("databaseUserName");
      String databasePassword = config().getString("databasePassword");
      int poolSize = config().getInteger("poolSize");
      Map<String, String> schemaProp = Map.of("search_path", databaseSchema);

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

      PoolOptions poolOptions = new PoolOptions().setMaxSize(poolSize);
      PgPool pool = PgPool.pool(vertx, connectOptions, poolOptions);

    PostgresService pgService = new PostgresServiceImpl(pool);

        binder = new ServiceBinder(vertx);
        consumer = binder.setAddress(PG_SERVICE_ADDRESS).register(PostgresService.class, pgService);
        LOGGER.info("Postgres verticle started.");
    }

    @Override
    public void stop() {
        binder.unregister(consumer);
    }
}