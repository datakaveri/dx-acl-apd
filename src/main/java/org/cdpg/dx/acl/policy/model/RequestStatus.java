package org.cdpg.dx.acl.policy.model;

import java.util.Arrays;
import java.util.Optional;

public enum RequestStatus {
  REJECTED("rejected"),
  PENDING("pending"),
  GRANTED("granted"),
  WITHDRAWN("withdrawn");

  private final String status;

  RequestStatus(String value) {
    this.status = value;
  }

  public String getRequestStatus() {
    return status;
  }

  @Override
  public String toString() {
    return status;
  }

  /**
   * Returns an Optional of RequestStatus based on the given status string.
   *
   * @param status the status string
   * @return Optional containing the matching RequestStatus, or empty if not found
   */
  public static Optional<RequestStatus> fromString(String status) {
    if (status == null || status.isEmpty()) {
      return Optional.empty();
    }
    return Arrays.stream(RequestStatus.values())
            .filter(s -> s.status.equalsIgnoreCase(status))
            .findFirst();
  }
}