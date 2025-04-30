package org.cdpg.dx.acl.policy.dao.impl;

import static org.cdpg.dx.acl.policy.dao.util.Constants.*;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.acl.policy.dao.PolicyMapperDao;
import org.cdpg.dx.acl.policy.dao.model.FetchPolicyDto;
import org.cdpg.dx.acl.policy.dao.model.PolicyResourceUrlDto;
import org.cdpg.dx.acl.policy.model.Role;
import org.cdpg.dx.common.models.User;
import org.cdpg.dx.database.postgres.models.*;
import org.cdpg.dx.database.postgres.service.PostgresService;

public class PolicyMapperDaoImpl implements PolicyMapperDao {

  private static final Logger LOGGER = LogManager.getLogger(PolicyMapperDaoImpl.class);
  SelectQuery selectQuery;
  InsertQuery insertQuery;
  UpdateQuery updateQuery;
  Condition condition;
  PostgresService postgresService;

  public PolicyMapperDaoImpl(
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
  public Future<List<FetchPolicyDto>> getPolicyForConsumerQuery(User consumer) {
    if(!consumer.getUserRole().equals(Role.CONSUMER)){
      return Future.failedFuture("User is not consumer");
    }
    Join userJoin = new Join(Join.JoinType.INNER, USER_TABLE, "U", "P." + DB_OWNER_ID, DB_ID);
    Join resourceJoin =
        new Join(Join.JoinType.INNER, RESOURCE_ENTITY_TABLE, "RE", "P." + DB_ITEM_ID, DB_ID);
    List<Join> joins = List.of(userJoin, resourceJoin);
    OrderBy orderBy = new OrderBy("P." + DB_UPDATED_AT, OrderBy.Direction.DESC);
      Condition consumerCondition =
          new Condition(
              "P." + DB_CONSUMER_EMAIL, Condition.Operator.EQUALS, List.of(consumer.getEmailId()));
    Condition resourceServerUrlCondition =
        new Condition(
            "RE." + DB_RESOURCE_SERVER_URL,
            Condition.Operator.EQUALS,
            List.of(consumer.getResourceServerUrl()));
    Condition condition = new Condition(List.of(consumerCondition,resourceServerUrlCondition), Condition.LogicalOperator.AND);
    List<String> columns =
        Arrays.asList(
            "'" + consumer.getUserId() + "' AS \"consumerId\"",
            "'" + consumer.getEmailId() + "' AS \"consumerEmailId\"",
            "'" + consumer.getFirstName() + "' AS \"consumerFirstName\"",
            "'" + consumer.getLastName() + "' AS \"consumerLastName\"",
            "P._id AS \"policyId\"",
            "P.item_id AS \"itemId\"",
            "RE.item_type AS \"itemType\"",
            "RE.resource_server_url AS \"resourceServerUrl\"",
            "P.owner_id AS \"ownerId\"",
            "U.first_name AS \"ownerFirstName\"",
            "U.last_name AS \"ownerLastName\"",
            "U.email_id AS \"ownerEmailId\"",
            "P.status AS \"status\"",
            "P.expiry_at AS \"expiryAt\"",
            "P.constraints AS \"constraints\"",
            "P.updated_at AS \"updatedAt\"",
            "P.created_at AS \"createdAt\"");


    SelectQuery selectQuery = new SelectQuery();
    selectQuery
        .setTable(POLICY_TABLE)
        .setTableAlias("P")
        .setCondition(condition)
        //              .setColumns(List.of("P."+DB_ID, "P." + DB_ITEM_ID, "RE." +
        // DB_RESOURCE_SERVER_URL, "P."+DB_OWNER_ID, "U."+DB_FIRST_NAME, "U."+DB_LAST_NAME,
        // "P."+DB_STATUS, "P."+DB_EXPIRY_AT, "P."+DB_CONSTRAINTS, "P."+DB_UPDATED_AT,
        // "P."+DB_CREATED_AT))
        .setColumns(columns)
        .setJoins(joins)
        .setOrderBy(List.of(orderBy));

    return postgresService
        .select(selectQuery)
        .compose(
            queryResult -> {
              JsonArray rows = queryResult.getRows();
              if (!rows.isEmpty()) {
                LOGGER.info("Policy Found");
                LOGGER.info("Policy : {}", rows.encodePrettily());
                List<FetchPolicyDto> list =
                    rows.stream().map(e -> new FetchPolicyDto(JsonObject.mapFrom(e))).toList();
                return Future.succeededFuture(list);
              } else {
                return Future.failedFuture("Policy Not Found");
              }
            })
        .recover(
            throwable -> Future.failedFuture(throwable.getMessage()));
  }

  @Override
  public Future<List<FetchPolicyDto>> getPolicyForProviderQuery(User provider) {
    if(!provider.getUserRole().equals(Role.PROVIDER)){
      return Future.failedFuture("User is not provider");
    }
    Join userJoin = new Join(Join.JoinType.LEFT, USER_TABLE, "U", "P." + DB_CONSUMER_EMAIL, DB_EMAIL_ID);
    Join resourceJoin =
        new Join(Join.JoinType.INNER, RESOURCE_ENTITY_TABLE, "RE", "P." + DB_ITEM_ID, DB_ID);
    List<Join> joins = List.of(userJoin, resourceJoin);
    OrderBy orderBy = new OrderBy("P." + DB_UPDATED_AT, OrderBy.Direction.DESC);

      Condition ownerCondition =
          new Condition("P." + DB_OWNER_ID, Condition.Operator.EQUALS, List.of(provider.getUserId()));
    Condition resourceServerUrlCondition =
        new Condition(
            "RE." + DB_RESOURCE_SERVER_URL,
            Condition.Operator.EQUALS,
            List.of(provider.getResourceServerUrl()));
    Condition condition = new Condition(List.of(ownerCondition,resourceServerUrlCondition), Condition.LogicalOperator.AND);

    List<String> columns =
        Arrays.asList(
            "'" + provider.getUserId() + "' AS \"ownerId\"",
            "'" + provider.getEmailId() + "' AS \"ownerEmailId\"",
            "'" + provider.getFirstName() + "' AS \"ownerFirstName\"",
            "'" + provider.getLastName() + "' AS \"ownerLastName\"",
            "P._id AS \"policyId\"",
            "P.item_id AS \"itemId\"",
            "RE.item_type AS \"itemType\"",
            "RE.resource_server_url AS \"resourceServerUrl\"",
            "P.user_emailid AS \"consumerEmailId\"",
            "U.first_name AS \"consumerFirstName\"",
            "U.last_name AS \"consumerLastName\"",
            "U._id AS \"consumerId\"",
            "P.status AS \"status\"",
            "P.expiry_at AS \"expiryAt\"",
            "P.constraints AS \"constraints\"",
            "P.updated_at AS \"updatedAt\"",
            "P.created_at AS \"createdAt\"");

    /**
     * consumerId, consumerEmailId, consumerFirstName, consumerLastName user.getUserId(),
     * user.getEmailId(), user.getFirstName(), user.getLastName()
     *
     * <p>List.of("'+user.getUserId()+ AS "consumerId" + ''", "'+user.getEmailId()+ AS
     * "consumerEmailId" +''", "'+user.getFirstName()+ AS "consumerFirstName" +''",
     * "'+user.getLastName()+ AS "consumerLastName" +'")
     */
    SelectQuery selectQuery = new SelectQuery();
    selectQuery
        .setTable(POLICY_TABLE)
        .setTableAlias("P")
        .setCondition(condition)
        .setColumns(columns)
        .setJoins(joins)
        .setOrderBy(List.of(orderBy));

    return postgresService
        .select(selectQuery)
        .compose(
            queryResult -> {
              JsonArray rows = queryResult.getRows();
              if (!rows.isEmpty()) {
                LOGGER.info("Policy Found");
                LOGGER.info("Policy : {}", rows.encodePrettily());
                List<FetchPolicyDto> list =
                    rows.stream().map(e -> new FetchPolicyDto(JsonObject.mapFrom(e))).toList();
                return Future.succeededFuture(list);
              } else {
                return Future.failedFuture("Policy Not Found");
              }
            })
        .recover(

            throwable ->{
              return Future.failedFuture(throwable.getMessage());

            }
        );
  }

  //    public static final String CHECK_IF_POLICY_PRESENT_QUERY =
  //      "SELECT p.owner_id, p.status, r.resource_server_url"
  //          + " FROM policy p"
  //          + " INNER JOIN resource_entity r ON p.item_id = r._id"
  //          + " WHERE p._id = $1;";

  @Override
  public Future<PolicyResourceUrlDto> checkIfPolicyIsPresentQuery(String policyId) {

    Join join =
        new Join(Join.JoinType.INNER, RESOURCE_ENTITY_TABLE, "RE", "P." + DB_ITEM_ID, DB_ID);

    Condition condition =
        new Condition("P." + DB_ID, Condition.Operator.EQUALS, List.of(policyId));

    SelectQuery selectQuery = new SelectQuery();
    selectQuery
        .setTable(POLICY_TABLE)
        .setTableAlias("P")
        .setCondition(condition)
        .setColumns(List.of("P." + DB_OWNER_ID, "P." + DB_STATUS, "RE." + DB_RESOURCE_SERVER_URL))
        .setJoins(List.of(join));


    return postgresService
        .select(selectQuery)
        .compose(
            queryResult -> {
              JsonArray rows = queryResult.getRows();
              if (!rows.isEmpty()) {
                LOGGER.info("Policy Found");
                LOGGER.info("Policy : {}", rows.encodePrettily());
                PolicyResourceUrlDto policyResourceUrlDto = new PolicyResourceUrlDto(rows.getJsonObject(0));
                return Future.succeededFuture(policyResourceUrlDto);
              } else {
                return Future.failedFuture("Policy Not Found");
              }
            })
        .recover(

            throwable ->{
              return Future.failedFuture(throwable.getMessage());

            }
        );  }
}
