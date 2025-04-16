package org.cdpg.dx.acl.dao;

import static org.cdpg.dx.acl.dao.util.Constants.*;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cdpg.dx.acl.dao.model.PolicyDto;
import org.cdpg.dx.database.postgres.models.Condition;
import org.cdpg.dx.database.postgres.models.InsertQuery;
import org.cdpg.dx.database.postgres.models.SelectQuery;
import org.cdpg.dx.database.postgres.models.UpdateQuery;
import org.cdpg.dx.database.postgres.service.PostgresService;

/**
 * Check where is the policy related queries to the table are done
 * What are the arguments required to query the table
 * what is the output type
 */
public class PolicyDaoImpl implements PolicyDao{
  SelectQuery selectQuery;
  InsertQuery insertQuery;
  UpdateQuery updateQuery;
  Condition condition;
  List<Object> value;
  PostgresService postgresService;

  public PolicyDaoImpl(){
    selectQuery = new SelectQuery();
    insertQuery = new InsertQuery();
    updateQuery = new UpdateQuery();
    condition = new Condition();
    value = new ArrayList<>();
  }
  public PolicyDaoImpl(SelectQuery selectQuery, InsertQuery insertQuery, UpdateQuery updateQuery, Condition condition){
    this.condition = condition;
    this.selectQuery = selectQuery;
    this.updateQuery = updateQuery;
    this.insertQuery = insertQuery;
  }
  @Override
  public Future<PolicyDto> getPolicyFromDb(String policyId, String ownerId, String consumerEmailId) {
    condition.setOperator(Condition.Operator.EQUALS);
    if(policyId != null){
      condition.setColumn("_id");
      value.add(policyId);
    }
    if(ownerId != null){
      condition.setColumn("owner_id");
      value.add(ownerId);
    }
    if(consumerEmailId != null){
      condition.setColumn("user_emailid");
      value.add(consumerEmailId);
    }
    condition.setValues(value);
    selectQuery.setColumns(List.of("*"));
    selectQuery.setTable(POLICY_TABLE);
    selectQuery.setCondition(condition);
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(DB_ID, policyId);
    paramMap.put(DB_OWNER_ID, ownerId);
    paramMap.put(DB_CONSUMER_EMAIL, consumerEmailId);


    postgresService.select(selectQuery).compose(queryResult -> {
      List<PolicyDto> policyDtos = new ArrayList<>();

    return null;
    });
    return null;
  }

  @Override
  public Future<PolicyDto> createPolicyInDb(JsonObject entry) {
    return null;
  }

  @Override
  public Future<PolicyDto> deletePolicyInDb(String policyId) {
    return null;
  }
}
