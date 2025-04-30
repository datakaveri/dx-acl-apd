package org.cdpg.dx.database.postgres.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeHelper {

  private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  // Returns current time as String using default pattern
  public static String getCurrentTimeString() {
    LocalDateTime now = LocalDateTime.now();
    return now.format(DEFAULT_FORMATTER);
  }

  // Returns current time as String with a custom pattern
  public static String getCurrentTimeString(String pattern) {
    DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern(pattern);
    LocalDateTime now = LocalDateTime.now();
    return now.format(customFormatter);
  }
}