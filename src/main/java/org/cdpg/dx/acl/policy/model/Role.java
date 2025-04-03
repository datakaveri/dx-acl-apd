package org.cdpg.dx.acl.policy.model;


import static org.cdpg.dx.common.models.ResponseUrn.ROLE_NOT_FOUND;

import java.util.stream.Stream;
import org.cdpg.dx.common.exception.DxRuntimeException;

public enum Role {
  PROVIDER("provider"),
  CONSUMER_DELEGATE("consumerDelegate"),
  PROVIDER_DELEGATE("providerDelegate"),
  CONSUMER("consumer");

  private final String role;

  Role(String value) {
    role = value;
  }

  public static Role fromString(String roleValue) {
    return Stream.of(values())
        .filter(element -> element.getRole().equalsIgnoreCase(roleValue))
        .findAny()
        .orElseThrow(() -> new DxRuntimeException(404, ROLE_NOT_FOUND));
  }

  public String getRole() {
    return role;
  }
}
