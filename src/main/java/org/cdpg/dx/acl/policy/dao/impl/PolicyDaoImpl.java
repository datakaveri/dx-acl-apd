package org.cdpg.dx.acl.policy.dao.impl;

import static org.cdpg.dx.acl.policy.dao.util.Constants.*;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.acl.policy.dao.PolicyDao;
import org.cdpg.dx.acl.policy.dao.model.PolicyDto;
import org.cdpg.dx.acl.policy.util.Status;
import org.cdpg.dx.database.postgres.models.*;
import org.cdpg.dx.database.postgres.service.PostgresService;

public class PolicyDaoImpl implements PolicyDao {
  private static final Logger LOGGER = LogManager.getLogger(PolicyDao.class);
  SelectQuery selectQuery;
  InsertQuery insertQuery;
  UpdateQuery updateQuery;
  Condition condition;
  PostgresService postgresService;

  public PolicyDaoImpl(
      PostgresService postgresService,
      SelectQuery selectQuery,
      InsertQuery insertQuery,
      UpdateQuery updateQuery,
      Condition condition) {
    this.selectQuery = selectQuery;
    this.updateQuery = updateQuery;
    this.insertQuery = insertQuery;
    this.condition = condition;
    this.postgresService = postgresService;
    selectQuery.setTable(POLICY_TABLE);
    updateQuery.setTable(POLICY_TABLE);
    insertQuery.setTable(POLICY_TABLE);
  }

  @Override
  public Future<PolicyDto> getActiveConsumerPolicyFromDB(String itemId, String consumerEmailId) {
    Condition condition1 = new Condition(DB_CONSUMER_EMAIL,Condition.Operator.EQUALS,List.of(consumerEmailId));
    Condition condition2 = new Condition(DB_ITEM_ID, Condition.Operator.EQUALS,List.of(itemId));
    Condition condition3 = new Condition(DB_EXPIRY_AT, Condition.Operator.GREATER, List.of(LocalDateTime.now()));
    Condition condition4 = new Condition(DB_STATUS, Condition.Operator.EQUALS,List.of(Status.ACTIVE.toString()));
    condition = new Condition((List.of(condition1,condition2,condition3,condition4)), Condition.LogicalOperator.AND);
    selectQuery.setColumns(List.of("*")).setCondition(condition);
    return postgresService
        .select(selectQuery)
        .compose(
            queryResult -> {
              PolicyDto policyDto = new PolicyDto(queryResult.getRows().getJsonObject(0));
              return Future.succeededFuture(policyDto);
            })
        .recover(
            throwable -> {
              throwable.printStackTrace();
              return Future.failedFuture(throwable.getMessage());
            });
  }

  @Override
  public Future<PolicyDto> checkExistingPolicyFromDb(
      String itemId, String ownerId, Status status, String consumerEmailId) {
    Condition condition1 = new Condition(DB_CONSUMER_EMAIL,Condition.Operator.EQUALS,List.of(consumerEmailId));
    Condition condition2 = new Condition(DB_OWNER_ID, Condition.Operator.EQUALS,List.of(ownerId));
    Condition condition3 = new Condition(DB_ITEM_ID, Condition.Operator.EQUALS, List.of(itemId));
    Condition condition4 = new Condition(DB_EXPIRY_AT, Condition.Operator.GREATER,List.of(LocalDateTime.now()));
    Condition condition5 = new Condition(DB_STATUS, Condition.Operator.EQUALS,List.of(status));
    condition = new Condition((List.of(condition1,condition2,condition3,condition4,condition5)), Condition.LogicalOperator.AND);
    selectQuery.setColumns(List.of("*")).setCondition(condition);
    return postgresService
        .select(selectQuery)
        .compose(
            queryResult -> {
              PolicyDto policyDto = new PolicyDto(queryResult.getRows().getJsonObject(0));
              return Future.succeededFuture(policyDto);
            })
        .recover(
            throwable -> {
              return Future.failedFuture(throwable.getMessage());
            });
  }

  @Override
  public Future<PolicyDto> createPolicyInDb(
      String consumerEmailId,
      String itemId,
      String ownerId,
      LocalDateTime expiryAt,
      JsonObject constraints) {
    Map<String, Object> queryParam =
        Map.of(
            DB_CONSUMER_EMAIL, consumerEmailId,
            DB_ITEM_ID, itemId,
            DB_OWNER_ID, ownerId,
            DB_EXPIRY_AT, expiryAt,
            DB_CONSTRAINTS, constraints,
            DB_STATUS, Status.ACTIVE);
    insertQuery
        .setColumns(new ArrayList<>(queryParam.keySet()))
        .setValues(new ArrayList<>(queryParam.values()));
    return postgresService
        .insert(insertQuery)
        .compose(
            queryResult -> {
              PolicyDto policyDto = new PolicyDto(queryResult.getRows().getJsonObject(0));
              return Future.succeededFuture(policyDto);
            })
        .recover(
            throwable -> {
              return Future.failedFuture(throwable.getMessage());
            });
  }

  @Override
  public Future<PolicyDto> deletePolicyInDb(String policyId) {
    updateQuery.setColumns(List.of(DB_STATUS)).setValues(List.of(Status.DELETED)).setCondition(condition.setColumn(DB_POLICY_ID).setValues(List.of(policyId)).setOperator(
        Condition.Operator.EQUALS)).setCondition(condition.setColumn(DB_EXPIRY_AT).setValues(List.of(LocalDateTime.now())).setOperator(
        Condition.Operator.GREATER));
    LOGGER.info("Update query is : {}", updateQuery.toSQL());
    return postgresService
        .update(updateQuery)
        .compose(
            queryResult -> {
              PolicyDto policyDto = new PolicyDto(queryResult.getRows().getJsonObject(0));
              return Future.succeededFuture(policyDto);
            })
        .recover(failure -> Future.failedFuture(failure.getMessage()));
  }
}
