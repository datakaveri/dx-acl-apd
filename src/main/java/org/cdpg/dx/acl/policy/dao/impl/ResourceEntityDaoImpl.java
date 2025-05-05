package org.cdpg.dx.acl.policy.dao.impl;

import static org.cdpg.dx.acl.policy.dao.util.Constants.*;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.cdpg.dx.acl.policy.dao.ResourceEntityDao;
import org.cdpg.dx.acl.policy.dao.model.ResourceEntityDto;
import org.cdpg.dx.database.postgres.models.Condition;
import org.cdpg.dx.database.postgres.models.InsertQuery;
import org.cdpg.dx.database.postgres.models.SelectQuery;
import org.cdpg.dx.database.postgres.models.UpdateQuery;
import org.cdpg.dx.database.postgres.service.PostgresService;



public class ResourceEntityDaoImpl implements ResourceEntityDao {
  SelectQuery selectQuery;
  InsertQuery insertQuery;
  UpdateQuery updateQuery;
  Condition condition;
  PostgresService postgresService;

  public ResourceEntityDaoImpl(
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
    selectQuery.setTable(RESOURCE_ENTITY_TABLE);
    updateQuery.setTable(RESOURCE_ENTITY_TABLE);
    insertQuery.setTable(RESOURCE_ENTITY_TABLE);
  }

  //    public static final String ENTITY_TABLE_CHECK =
  //      "Select _id,provider_id,item_type,resource_server_url from resource_entity where _id = ANY
  // ($1::UUID[]);";
  // TODO: where _id = ANY
  //  // ($1::UUID[]);";
  @Override
  public Future<List<ResourceEntityDto>> getResourceFromDb(String... resourceIds) {
    Condition condition1 = new Condition(DB_ITEM_TYPE,Condition.Operator.IN, List.of(resourceIds));
    selectQuery
        .setColumns(List.of("*"))
        .setCondition(condition1);
    return postgresService
        .select(selectQuery)
        .compose(
            queryResult -> {
              List<ResourceEntityDto> list =
                  queryResult.getRows().stream().map(e -> new ResourceEntityDto(JsonObject.mapFrom(e))).toList();
              return Future.succeededFuture(list);
            })
        .recover(
            throwable -> {
              return Future.failedFuture(throwable.getMessage());
            });
    
    
  }

  //    public static final String INSERT_ENTITY_TABLE =
  //      "insert into
  // resource_entity(_id,provider_id,resource_group_id,item_type,resource_server_url)"
  //          + " values ($1,$2,$3,$4,$5);";

  //    public static final String INSERT_RESOURCE_INFO_QUERY = "INSERT INTO resource_entity "
  //          + "(_id, provider_id, resource_group_id,resource_server_url,item_type)"
  //          + " VALUES ($1::uuid, $2::uuid, $3::uuid,$4,$5) ON CONFLICT (_id) DO NOTHING;";

  //TODO: on conflict do nothing
  @Override
  public Future<ResourceEntityDto> createResourceInDb(
      String resourceId,
      String providerId,
      String resourceGroupId,
      String itemType,
      String resourceServerUrl,
      boolean onConflictDoNothing) {
    Map<String, Object> queryParam =
        Map.of(
            DB_ID, resourceId,
            DB_PROVIDER_ID, providerId,
            DB_RESOURCE_GROUP_ID, resourceGroupId,
            DB_ITEM_TYPE, itemType,
            DB_RESOURCE_SERVER_URL, resourceServerUrl);


    insertQuery
        .setColumns(new ArrayList<>(queryParam.keySet()))
        .setValues(new ArrayList<>(queryParam.values()));
    return postgresService
        .insert(insertQuery)
        .compose(
            queryResult -> {
              ResourceEntityDto resourceEntityDto =
                  new ResourceEntityDto(queryResult.getRows().getJsonObject(0));
              return Future.succeededFuture(resourceEntityDto);
            })
        .recover(
            throwable -> {
              return Future.failedFuture(throwable.getMessage());
            });
  }

  //    public static final String OWNERSHIP_CHECK_QUERY =
  //          "SELECT * FROM resource_entity WHERE _id = $1::uuid AND provider_id = $2::uuid";
  @Override
  public Future<ResourceEntityDto> checkResourcesFromDb(String resourceId, String providerId) {
    Condition resourceIdCondition = new Condition(DB_ID,Condition.Operator.EQUALS,List.of(resourceId));
    Condition providerIdCondition = new Condition(DB_PROVIDER_ID, Condition.Operator.EQUALS,List.of(providerId));
    condition = new Condition((List.of(resourceIdCondition,providerIdCondition)), Condition.LogicalOperator.AND);
    selectQuery.setColumns(List.of("*")).setCondition(condition);
    return postgresService
        .select(selectQuery)
        .compose(
            queryResult -> {
              ResourceEntityDto resourceEntityDto = new ResourceEntityDto(queryResult.getRows().getJsonObject(0));
              return Future.succeededFuture(resourceEntityDto);
            })
        .recover(
            throwable -> {
              return Future.failedFuture(throwable.getMessage());
            });
  }
}
