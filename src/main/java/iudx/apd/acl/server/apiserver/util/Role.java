package iudx.apd.acl.server.apiserver.util;

import static iudx.apd.acl.server.common.ResponseUrn.ROLE_NOT_FOUND;

import iudx.apd.acl.server.validation.exceptions.DxRuntimeException;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Role {
  PROVIDER("provider"),
  CONSUMER_DELEGATE("consumerDelegate"),
  PROVIDER_DELEGATE("providerDelegate"),
  CONSUMER("consumer");

  private final String role;
  private static final Logger LOGGER = LoggerFactory.getLogger(Role.class);

  Role(String value) {
    role = value;
  }

  public static Role fromString(String roleValue) {
    return Stream.of(values())
        .filter(element -> element.getRole().equalsIgnoreCase(roleValue))
        .findAny()
        .orElseThrow(() ->{
            // Throwing a custom exception if the role is not found
          LOGGER.error("Role not found: {}", roleValue);
          return new DxRuntimeException(404, ROLE_NOT_FOUND);
        });
  }

  public String getRole() {
    return role;
  }
}
