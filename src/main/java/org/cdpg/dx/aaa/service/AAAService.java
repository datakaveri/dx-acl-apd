package org.cdpg.dx.aaa.service;

import io.vertx.core.Future;
import org.cdpg.dx.aaa.models.UserInfo;
import org.cdpg.dx.common.models.User;;;


/**
 * Service interface for handling user authentication and fetching user information.
 */
public interface AAAService {
  Future<User> fetchUserInfo(UserInfo userInfo);
}