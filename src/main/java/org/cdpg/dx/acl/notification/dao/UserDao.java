package org.cdpg.dx.acl.notification.dao;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.Future;
import java.util.List;
import org.cdpg.dx.acl.notification.dao.model.UserDto;


public interface UserDao {
  public Future<UserDto> getUserFromDb(String userId);
  public Future<UserDto> createUserInDb(String userId, String emailId, String firstName, String lastName);
  public Future<List<UserDto>> getUsersFromDb(String emailId, String userId);


}
