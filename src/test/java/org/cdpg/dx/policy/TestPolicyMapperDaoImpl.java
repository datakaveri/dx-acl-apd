package org.cdpg.dx.policy;

import static iudx.apd.acl.server.apiserver.util.Constants.*;
import static iudx.apd.acl.server.apiserver.util.Constants.FIRST_NAME;
import static iudx.apd.acl.server.apiserver.util.Constants.LAST_NAME;
import static iudx.apd.acl.server.apiserver.util.Constants.RS_SERVER_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.acl.policy.dao.PolicyMapperDao;
import org.cdpg.dx.acl.policy.dao.impl.PolicyMapperDaoImpl;
import org.cdpg.dx.acl.policy.dao.model.FetchPolicyDto;
import org.cdpg.dx.acl.policy.dao.model.PolicyResourceUrlDto;
import org.cdpg.dx.common.models.User;
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
public class TestPolicyMapperDaoImpl {
  private static final Logger LOGGER = LogManager.getLogger(TestPolicyMapperDaoImpl.class);
  PostgresService postgresService;
  InsertQuery insertQuery;
  UpdateQuery updateQuery;
  SelectQuery selectQuery;
  Util util;
  Condition condition;
  @Container PostgreSQLContainer container = new PostgreSQLContainer<>("postgres:12.11");
  private PolicyMapperDao policyMapperDao;

  @BeforeEach
  public void setUp(VertxTestContext vertxTestContext) {
    this.util = new Util();
    postgresService = util.setUp(container);
    insertQuery = new InsertQuery();
    updateQuery = new UpdateQuery();
    selectQuery = new SelectQuery();
    condition = new Condition();
    policyMapperDao =
        new PolicyMapperDaoImpl(postgresService, selectQuery, insertQuery, updateQuery, condition);
    vertxTestContext.completeNow();
  }

  private User getConsumer() {
    JsonObject jsonObject =
        new JsonObject()
            .put(USER_ID, util.getConsumerId())
            .put(USER_ROLE, "consumer")
            .put(EMAIL_ID, util.getConsumerEmailId())
            .put(FIRST_NAME, util.getConsumerFirstName())
            .put(RS_SERVER_URL, "rs.iudx.io")
            .put(LAST_NAME, util.getConsumerLastName());
    return new User(jsonObject);
  }

  private User getProvider() {
    JsonObject jsonObject =
        new JsonObject()
            .put(USER_ID, util.getOwnerId())
            .put(USER_ROLE, "provider")
            .put(EMAIL_ID, util.getOwnerEmailId())
            .put(FIRST_NAME, util.getOwnerFirstName())
            .put(RS_SERVER_URL, "rs.iudx.io")
            .put(LAST_NAME, util.getOwnerLastName());
    return new User(jsonObject);
  }

  @Test
  @DisplayName("Test getPolicyForUserQuery method : Success")
  public void testGetPolicyForUserQuery(VertxTestContext vertxTestContext) {
    util.testInsert()
        .onComplete(
            result -> {
              if (result.succeeded()) {
                policyMapperDao
                    .getPolicyForConsumerQuery(getConsumer())
                    .onComplete(
                        handler -> {
                          if (handler.succeeded()) {
                            FetchPolicyDto row = handler.result().getFirst();
                            assertEquals(util.getConsumerEmailId(), row.getConsumer().getEmail());
                            assertEquals(util.getResourceId().toString(), row.getItemId());
                            assertEquals(
                                util.getConsumerId().toString(), row.getConsumer().getId());
                            assertEquals(util.getPolicyId().toString(), row.getPolicyId());
                            assertEquals(util.getOwnerId().toString(), row.getProvider().getId());
                            assertEquals(util.getOwnerEmailId(), row.getProvider().getEmail());
                            assertEquals(
                                util.getOwnerFirstName(),
                                row.getProvider().getName().getFirstName());
                            assertEquals(
                                util.getOwnerLastName(), row.getProvider().getName().getLastName());
                            assertEquals(
                                util.getConsumerFirstName(),
                                row.getConsumer().getName().getFirstName());
                            assertEquals(
                                util.getConsumerLastName(),
                                row.getConsumer().getName().getLastName());
                            assertEquals("rs.iudx.io", row.getResourceServerUrl());
                            assertEquals(util.getStatus(), row.getStatus());
                            assertEquals(util.getConstraints(), row.getConstraints());
                            vertxTestContext.completeNow();
                          } else {
                            vertxTestContext.failNow(
                                "Failed to get consumer policies : "
                                    + handler.cause().getMessage());
                          }
                        });
              } else {
                vertxTestContext.failNow("Failed to insert in the database to update the info");
              }
            });
  }

  @Test
  @DisplayName("Test getPolicyForProviderQuery method : Success")
  public void testGetPolicyForProviderQuery(VertxTestContext vertxTestContext) {
    util.testInsert()
        .onComplete(
            result -> {
              if (result.succeeded()) {
                policyMapperDao
                    .getPolicyForProviderQuery(getProvider())
                    .onComplete(
                        handler -> {
                          if (handler.succeeded()) {
                            FetchPolicyDto row = handler.result().getFirst();
                            assertEquals(util.getConsumerEmailId(), row.getConsumer().getEmail());
                            assertEquals(util.getResourceId().toString(), row.getItemId());
                            assertEquals(
                                util.getConsumerId().toString(), row.getConsumer().getId());
                            assertEquals(util.getPolicyId().toString(), row.getPolicyId());
                            assertEquals(util.getOwnerId().toString(), row.getProvider().getId());
                            assertEquals(util.getOwnerEmailId(), row.getProvider().getEmail());
                            assertEquals(
                                util.getOwnerFirstName(),
                                row.getProvider().getName().getFirstName());
                            assertEquals(
                                util.getOwnerLastName(), row.getProvider().getName().getLastName());
                            assertEquals(
                                util.getConsumerFirstName(),
                                row.getConsumer().getName().getFirstName());
                            assertEquals(
                                util.getConsumerLastName(),
                                row.getConsumer().getName().getLastName());
                            assertEquals("rs.iudx.io", row.getResourceServerUrl());
                            assertEquals(util.getStatus(), row.getStatus());
                            assertEquals(util.getConstraints(), row.getConstraints());
                            vertxTestContext.completeNow();
                          } else {
                            vertxTestContext.failNow(
                                "Failed to get provider policies : "
                                    + handler.cause().getMessage());
                          }
                        });
              } else {
                vertxTestContext.failNow("Failed to insert in the database to update the info");
              }
            });
  }

  @Test
  @DisplayName("Test checkIfPolicyIsPresentQuery method : Success")
  public void testCheckIfPolicyIsPresentQuery(VertxTestContext vertxTestContext) {
    util.testInsert()
        .onComplete(
            result -> {
              if (result.succeeded()) {
                policyMapperDao
                    .checkIfPolicyIsPresentQuery(util.getPolicyId().toString())
                    .onComplete(
                        handler -> {
                          if (handler.succeeded()) {
                            PolicyResourceUrlDto row = handler.result();
                            assertEquals(util.getOwnerId().toString(), row.getOwnerId());
                            assertEquals("rs.iudx.io", row.getResourceServerUrl());
                            assertEquals(util.getStatus(), row.getStatus().toString());
                            vertxTestContext.completeNow();
                          } else {
                            vertxTestContext.failNow(
                                "Failed to get policy : " + handler.cause().getMessage());
                          }
                        });
              } else {
                vertxTestContext.failNow("Failed to insert in the database to update the info");
              }
            });
  }
}
