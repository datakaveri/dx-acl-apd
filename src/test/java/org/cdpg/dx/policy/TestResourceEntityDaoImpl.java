package org.cdpg.dx.policy;


import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.acl.policy.dao.impl.ResourceEntityDaoImpl;
import org.cdpg.dx.acl.policy.dao.model.ResourceEntityDto;
import org.cdpg.dx.database.postgres.Util;
import org.cdpg.dx.database.postgres.models.*;
import org.cdpg.dx.database.postgres.service.PostgresService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ExtendWith({MockitoExtension.class, VertxExtension.class})
public class TestResourceEntityDaoImpl {
  private static final Logger LOGGER = LogManager.getLogger(TestResourceEntityDaoImpl.class);
  PostgresService postgresService;
  InsertQuery insertQuery;
  UpdateQuery updateQuery;
  SelectQuery selectQuery;
  Util util;
  Condition condition;
  @Container PostgreSQLContainer container = new PostgreSQLContainer<>("postgres:12.11");
  private ResourceEntityDaoImpl resourceEntityDao;

  @BeforeEach
  public void setUp(VertxTestContext vertxTestContext) {
    this.util = new Util();
    postgresService = util.setUp(container);
    insertQuery = new InsertQuery();
    updateQuery = new UpdateQuery();
    selectQuery = new SelectQuery();
    condition = new Condition();
    resourceEntityDao =
        new ResourceEntityDaoImpl(
            postgresService, selectQuery, insertQuery, updateQuery, condition);
    vertxTestContext.completeNow();
  }

  @Test
  @DisplayName("Test getActiveConsumerPolicyFromDB method : Success")
  public void testGetActiveConsumerPolicyFromDB(VertxTestContext vertxTestContext) {
    util.testInsert()
        .onComplete(
            result -> {
              if (result.succeeded()) {
                resourceEntityDao
                    .getResourceFromDb(
                        util.getResourceId().toString(), util.getResourceGroupId().toString())
                    .onComplete(
                        handler -> {
                          if (handler.succeeded()) {
                            List<ResourceEntityDto> policyDto = handler.result();

                            vertxTestContext.completeNow();
                          } else {
                            vertxTestContext.failNow(
                                "Failed to get resource : "
                                    + handler.cause().getMessage());
                          }
                        });
              } else {
                vertxTestContext.failNow("Failed to insert in the database to update the info");
              }
            });
  }

}
