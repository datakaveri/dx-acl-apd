package org.cdpg.dx.acl.notification.dao.impl;

import io.vertx.core.Future;
import java.util.List;
import org.cdpg.dx.acl.notification.dao.UserDao;
import org.cdpg.dx.acl.notification.dao.model.UserDto;
import org.cdpg.dx.database.postgres.models.Condition;
import org.cdpg.dx.database.postgres.models.InsertQuery;
import org.cdpg.dx.database.postgres.models.SelectQuery;
import org.cdpg.dx.database.postgres.models.UpdateQuery;

public class UserDaoImpl implements UserDao {
  SelectQuery selectQuery;
  InsertQuery insertQuery;
  UpdateQuery updateQuery;
  Condition condition;

  public UserDaoImpl(){
    selectQuery = new SelectQuery();
    insertQuery = new InsertQuery();
    updateQuery = new UpdateQuery();
    condition = new Condition();
  }
  public UserDaoImpl(SelectQuery selectQuery, InsertQuery insertQuery, UpdateQuery updateQuery, Condition condition){
    this.condition = condition;
    this.selectQuery = selectQuery;
    this.updateQuery = updateQuery;
    this.insertQuery = insertQuery;
  }

  @Override
  public Future<UserDto> getUserFromDb(String userId) {
    return null;
  }

  @Override
  public Future<UserDto> createUserInDb(String userId, String emailId, String firstName, String lastName) {
    return null;
  }

  @Override
  public Future<List<UserDto>> getUsersFromDb(String emailId, String userId) {
    return null;
  }
}
