package org.cdpg.dx.acl.policy.repository;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import iudx.apd.acl.server.apiserver.ApiServerVerticle;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.acl.policy.model.Policy;
import org.cdpg.dx.acl.policy.model.PolicyUpdateDTO;
import org.cdpg.dx.acl.policy.util.Constants;
import org.cdpg.dx.database.postgres.models.*;
import org.cdpg.dx.database.postgres.service.PostgresService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PolicyDAOImpl implements PolicyDAO {
    private final PostgresService postgresService;
  private static final Logger LOGGER = LogManager.getLogger(PolicyDAOImpl.class);

    public PolicyDAOImpl(PostgresService postgresService) {
      LOGGER.info("heree :{} ", postgresService);
        this.postgresService = postgresService;
    }

    @Override
    public Future<Policy> create(Policy policy) {
    Map<String, Object> policyMap = policy.toJson().getMap();

        List<String> columns = policyMap.keySet().stream().toList();
        List<Object> values = policyMap.values().stream().toList();

        InsertQuery query = new InsertQuery(Constants.POLICY_TABLE, columns, values);

    return postgresService
        .insert(query)
        .compose(
            result -> {
              if (result.getRows().isEmpty()) {
                return Future.failedFuture("Insert query returned no rows.");
              }
              return Future.succeededFuture(new Policy(result.getRows().getJsonObject(0)));
            })
        .recover(
            err -> {
              System.err.println("Error inserting policy: " + err.getMessage());
              return Future.failedFuture(err);
            });
    }

    @Override
    public Future<Boolean> update(String id, PolicyUpdateDTO updateDTO) {
        Map<String, Object> feildsMap = updateDTO.toNonEmptyFieldsMap();

        if (feildsMap.isEmpty()) {
            return Future.failedFuture(new IllegalArgumentException("No fields to update"));
        }

        List<String> columns = List.copyOf(feildsMap.keySet());
        List<Object> values = List.copyOf(feildsMap.values());

        // Create Condition for WHERE clause
        Condition condition = new Condition(Constants.POLICY_ID, Condition.Operator.EQUALS, List.of(id));

        // Build the UpdateQuery
        UpdateQuery query = new UpdateQuery(Constants.POLICY_TABLE, columns, values, condition, null, null);


        return postgresService.update(query)
                .compose(result -> {
                    if (!result.isRowsAffected()) {
                        return Future.failedFuture("No rows updated");
                    }
                    return Future.succeededFuture(true);
                })
                .recover(err -> {
                    System.err.println("Error updating policy: "+ id.toString() + " msg:"+ err.getMessage());
                    return Future.failedFuture(err);
                });
    }
    @Override
    public Future<Policy> getById(String id) {

        List<String> columns = Constants.ALL_POLICY_FIELDS;;
        // Create Condition for WHERE clause
        Condition condition = new Condition(Constants.POLICY_ID, Condition.Operator.EQUALS, List.of(id));


        SelectQuery query = new SelectQuery(Constants.POLICY_TABLE, columns,condition,null, null,null,null);

    return postgresService
        .select(query)
        .compose(
            result -> {
              if (result.getRows().isEmpty()) {
                return Future.failedFuture("select query returned no rows.");
              }
              return Future.succeededFuture(new Policy(result.getRows().getJsonObject(0)));
            })
        .recover(
            err -> {
              System.err.println("Error inserting policy: " + err.getMessage());
              return Future.failedFuture(err);
            });
    }

    @Override
    public Future<Boolean> delete(String id) {

    //         Create Condition for WHERE clause
//        Condition condition = new Condition(Constants.POLICY_ID, Condition.Operator.EQUALS, List.of(id));
        Condition condition1 = new Condition();
        condition1.setColumn(Constants.POLICY_ID);
        condition1.setValues(List.of(id));
        condition1.setOperator(Condition.Operator.EQUALS);


        // Build the UpdateQuery
//        DeleteQuery query = new DeleteQuery(Constants.POLICY_TABLE, component, null, null);
//        DeleteQuery query = new DeleteQuery(new JsonObject().put("table", Constants.POLICY_TABLE).put("condition", component).put("orderBy", null).put("limit", null));
        DeleteQuery query = new DeleteQuery();
        query.setCondition(condition1);
        query.setLimit(null);
        query.setTable(Constants.POLICY_TABLE);
    query.setOrderBy(new ArrayList<>());

    return postgresService
        .delete(query)
        .compose(
            result -> {
              if (!result.isRowsAffected()) {
                return Future.failedFuture("No rows updated");
              }
              return Future.succeededFuture(true);
            })
        .recover(
            err -> {
              System.err.println(
                  "Error updating policy: " + id.toString() + " msg:" + err.getMessage());
              LOGGER.error("What's the query : {}", query);
              return Future.failedFuture(err);
            });
    }
}
