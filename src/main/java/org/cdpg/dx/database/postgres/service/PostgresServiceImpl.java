package org.cdpg.dx.database.postgres.service;

import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.templates.SqlTemplate;
import java.util.Map;
import org.cdpg.dx.acl.dao.model.PolicyDto;
import org.cdpg.dx.database.postgres.models.*;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

import java.util.List;

public class PostgresServiceImpl implements PostgresService {
    private final Pool client;

    public PostgresServiceImpl(Pool client) {
        this.client = client;
    }

    private QueryResult convertToQueryResult(RowSet<Row> rowSet) {
        JsonArray jsonArray = new JsonArray();

        for (Row row : rowSet) {
            JsonObject json = new JsonObject();
            for (int i = 0; i < row.size(); i++) {
                json.put(row.getColumnName(i), row.getValue(i));
            }
            jsonArray.add(json);
        }

        boolean rowsAffected = rowSet.rowCount() > 0; // Check if any rows were affected

        QueryResult result = new QueryResult();
        result.setRows(jsonArray);
        result.setHasMore(false);
        result.setTotalCount(jsonArray.size());
        result.setRowsAffected(rowsAffected);
        return result;
    }

    private Future<QueryResult> executeQuery(String sql, List<Object> params) {
        return client.preparedQuery(sql).execute(Tuple.from(params))
            .map(this::convertToQueryResult);
    }

//    public Future executeQuery(String sql, Object param, Class<?> tClass){
//        PolicyDto policyDto = new PolicyDto();
//        policyDto.setConstraints(null);
//        SqlTemplate.forQuery(client,sql).mapFrom(tClass).execute(param,handler -> {
//
//            })
//            .map(this::convertToQueryResult);
//
//    return null;
//    }

//    @Override
//    public Future<QueryResult> execute(Query query) {
//        return executeQuery(query.toSQL(), query.getQueryParams());
//    }

    @Override
    public Future<QueryResult> insert(InsertQuery query) {
        return executeQuery(query.toSQL(), query.getQueryParams());
    }

    @Override
    public Future<QueryResult> update(UpdateQuery query) {
        return executeQuery(query.toSQL(), query.getQueryParams());
    }

    @Override
    public Future<QueryResult> delete(DeleteQuery query) {
        return executeQuery(query.toSQL(), query.getQueryParams());
    }

    @Override
    public Future<QueryResult> select(SelectQuery query) {
        return executeQuery(query.toSQL(), query.getQueryParams());
    }
}
