package org.cdpg.dx.database.postgres.service;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.cdpg.dx.database.postgres.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgresServiceImpl implements PostgresService {
    private final Pool client;
    private static final Logger LOG = LoggerFactory.getLogger(PostgresServiceImpl.class);

    public PostgresServiceImpl(Pool client) {
        this.client = client;
    }

    private QueryResult convertToQueryResult(RowSet<Row> rowSet) {
      System.out.println("Inside convertToQueryResult");
      JsonArray jsonArray = new JsonArray();
        Object value;
        for (Row row : rowSet) {
          JsonObject json = new JsonObject();
            for (int i = 0; i < row.size(); i++) {
               // json.put(row.getColumnName(i), row.getValue(i));
              LOG.info("Column name: " + row.getColumnName(i) + ", value: " + row.getValue(i));
              String column = row.getColumnName(i);
              value = row.getValue(i);
              if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof JsonObject || value instanceof JsonArray) {
                System.out.println("value:"+value);
                json.put(column, value);
              } else {
                json.put(column, value.toString());
              }
            }
          jsonArray.add(json);
        }


        boolean rowsAffected = rowSet.rowCount() > 0; // Check if any rows were affected
        if(rowsAffected)
      {
        System.out.println("Rows affected :"+rowSet.rowCount());
      }
      else
      {
        System.out.println("Rows unaffected");
      }
      System.out.println("Returned rows: " + jsonArray.encodePrettily());

      QueryResult queryResult = new QueryResult();
      queryResult.setRows(jsonArray);
      queryResult.setTotalCount(rowSet.rowCount());
      queryResult.setHasMore(false);
      queryResult.setRowsAffected(rowsAffected);
      //return new QueryResult(jsonArray, jsonArray.size(), false, rowsAffected);
      return queryResult;
    }

  private Future<QueryResult> executeQuery(String sql, List<Object> params) {
    System.out.println("Executing SQL: " + sql);
    System.out.println("With parameters: " + params);
    Tuple tuple = Tuple.tuple();


    try {




      for (Object param : params) {
        System.out.println("Param type: " + (param != null ? param.getClass().getSimpleName() : "null") + ", value: " + param);


        if (param instanceof String) {
          String paramStr = (String) param;


          // Check if it's an ISO timestamp string
          if (paramStr.matches("\\d{4}-\\d{2}-\\d{2}T.*Z")) {
            try {
              // Parse and
              // qconvert to LocalDateTime
              LocalDateTime time = ZonedDateTime.parse(paramStr).toLocalDateTime();
              tuple.addValue(time);
              continue;
            } catch (Exception e) {
              System.out.println("Failed to parse timestamp, keeping as string: " + paramStr);
            }
          }
        }
          // Default: keep original
          tuple.addValue(param);

      }





      return client
        .preparedQuery(sql)
        .execute(tuple)
        .map(rowSet -> {
          System.out.println("Query executed successfully.");
          return convertToQueryResult(rowSet);
        })
        .onFailure(err -> {
          System.err.println("SQL execution error: " + err.getMessage());
          err.printStackTrace();
        });


    } catch (Exception e) {
      System.err.println("Exception while building Tuple or executing query: " + e.getMessage());
      e.printStackTrace();
      return Future.failedFuture("Error in PostgresServiceImpl: " + e.getMessage());
    }
  }


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
