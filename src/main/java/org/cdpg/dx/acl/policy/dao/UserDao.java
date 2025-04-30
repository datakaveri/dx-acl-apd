package org.cdpg.dx.acl.policy.dao;

import io.vertx.core.Future;
import java.util.List;
import org.cdpg.dx.acl.policy.dao.model.UserDto;

public interface UserDao {
  public Future<UserDto> getUserFromDb(String userId);
  public Future<UserDto> createUserInDb(String userId, String emailId, String firstName, String lastName);
  public Future<List<UserDto>> getUsersFromDb(String emailId, String userId);


}
