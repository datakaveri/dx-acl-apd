package org.cdpg.dx.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.acl.policy.dao.impl.PolicyDaoImpl;
import org.cdpg.dx.acl.policy.dao.model.PolicyDto;
import org.cdpg.dx.acl.policy.util.Status;
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
public class TestPolicyDaoImpl {
  private static final Logger LOGGER = LogManager.getLogger(TestPolicyDaoImpl.class);
  PostgresService postgresService;
  InsertQuery insertQuery;
  UpdateQuery updateQuery;
  SelectQuery selectQuery;
  Util util;
  Condition condition;
  @Container PostgreSQLContainer container = new PostgreSQLContainer<>("postgres:12.11");
  private PolicyDaoImpl policyDao;

  @BeforeEach
  public void setUp(VertxTestContext vertxTestContext) {
    this.util = new Util();
    postgresService = util.setUp(container);
    insertQuery = new InsertQuery();
    updateQuery = new UpdateQuery();
    selectQuery = new SelectQuery();
    condition = new Condition();
    policyDao =
        new PolicyDaoImpl(postgresService, selectQuery, insertQuery, updateQuery, condition);
    vertxTestContext.completeNow();
  }

  @Test
  @DisplayName("Test getActiveConsumerPolicyFromDB method : Success")
  public void testGetActiveConsumerPolicyFromDB(VertxTestContext vertxTestContext) {
    util.testInsert()
        .onComplete(
            result -> {
              if (result.succeeded()) {
                policyDao
                    .getActiveConsumerPolicyFromDB(
                        util.getResourceId().toString(), util.getConsumerEmailId())
                    .onComplete(
                        handler -> {
                          if (handler.succeeded()) {
                            PolicyDto policyDto = handler.result();
                            assertEquals(util.getConsumerEmailId(), policyDto.getConsumerEmailId());
                            assertEquals(util.getResourceId().toString(), policyDto.getItemId());
                            assertEquals(util.getStatus(), policyDto.getPolicyStatus());
                            vertxTestContext.completeNow();
                          } else {
                            vertxTestContext.failNow(
                                "Failed to get active consumer policies : "
                                    + handler.cause().getMessage());
                          }
                        });
              } else {
                vertxTestContext.failNow("Failed to insert in the database to update the info");
              }
            });
  }

  @Test
  @DisplayName("Test checkExistingPolicyFromDb method : Success")
  public void testCheckExistingPolicyFromDb(VertxTestContext vertxTestContext) {
    util.testInsert()
        .onComplete(
            result -> {
              if (result.succeeded()) {
                policyDao
                    .checkExistingPolicyFromDb(
                        util.getResourceId().toString(),
                        util.getOwnerId().toString(),
                        Status.valueOf(util.getStatus()),
                        util.getConsumerEmailId())
                    .onComplete(
                        handler -> {
                          if (handler.succeeded()) {
                            PolicyDto policyDto = handler.result();
                            assertEquals(util.getConsumerEmailId(), policyDto.getConsumerEmailId());
                            assertEquals(util.getResourceId().toString(), policyDto.getItemId());
                            assertEquals(util.getStatus(), policyDto.getPolicyStatus());
                            vertxTestContext.completeNow();
                          } else {
                            vertxTestContext.failNow(
                                "Failed to check if a policy is present : "
                                    + handler.cause().getMessage());
                          }
                        });
              } else {
                vertxTestContext.failNow("Failed to insert in the database to update the info");
              }
            });
  }

  @Test
  @DisplayName("Test createPolicyInDb method")
  public void testCreatePolicyInDbSuccess(VertxTestContext vertxTestContext) {
    JsonObject constraints = new JsonObject().put("access", List.of("file", "sub", "async"));
    util.testInsert()
        .onComplete(
            result -> {
              if (result.succeeded()) {
                policyDao
                    .createPolicyInDb(
                        util.getConsumerEmailId(),
                        util.getResourceId().toString(),
                        util.getOwnerId().toString(),
                        LocalDateTime.now(),
                        constraints)
                    .onComplete(
                        handler -> {
                          if (handler.succeeded()) {
                            vertxTestContext.completeNow();
                          } else {
                            vertxTestContext.failNow(
                                "Failed to create a policy : " + handler.cause().getMessage());
                          }
                        });
              } else {
                vertxTestContext.failNow("Failed to insert in the database to update the info");
              }
            });
  }

  @Test
  @DisplayName("Test deletePolicyInDb method")
  public void testDeletePolicyInDb(VertxTestContext vertxTestContext) {
    util.testInsert()
        .onComplete(
            result -> {
              if (result.succeeded()) {
                policyDao
                    .deletePolicyInDb(util.getPolicyId().toString())
                    .onComplete(
                        handler -> {
                          if (handler.succeeded()) {
                            PolicyDto dbResult = handler.result();
                            assertEquals(util.getPolicyId().toString(), dbResult.getPolicyId());
                            assertEquals(util.getResourceId().toString(), dbResult.getItemId());
                            assertEquals(util.getConsumerEmailId(), dbResult.getConsumerEmailId());
                            assertEquals(Status.DELETED.toString(), dbResult.getPolicyStatus());
                            vertxTestContext.completeNow();
                          } else {
                            vertxTestContext.failNow(
                                "Failed to delete : " + handler.cause().getMessage());
                          }
                        });
              } else {
                vertxTestContext.failNow("Failed to insert in the database to update the info");
              }
            });
  }
}
