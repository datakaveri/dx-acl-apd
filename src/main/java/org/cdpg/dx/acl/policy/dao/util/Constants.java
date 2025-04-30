package org.cdpg.dx.acl.policy.dao.util;

import java.util.List;

/**
 * Constants for database
 */
public class Constants {
  public static final String DB_ID = "_id";
  public static final String DB_CONSUMER_EMAIL = "user_emailid";
  public static final String DB_ITEM_ID = "item_id";
  public static final String DB_OWNER_ID = "owner_id";
  public static final String DB_STATUS = "status";
  public static final String DB_EXPIRY_AT = "expiry_at";
  public static final String DB_CREATED_AT = "created_at";
  public static final String DB_UPDATED_AT = "updated_at";
  public static final String DB_CONSTRAINTS = "constraints";
  public static final String DB_EMAIL_ID = "email_id";
  public static final String DB_FIRST_NAME = "first_name";
  public static final String DB_LAST_NAME = "last_name";
  public static final String DB_ADDITIONAL_INFO = "additional_info";
  public static final String DB_USER_ID = "user_id";
  public static final String DB_REQUEST_ID = "request_id";
  public static final String DB_POLICY_ID = "_id";
  public static final String DB_PROVIDER_ID = "provider_id";
  public static final String DB_RESOURCE_GROUP_ID = "resource_group_id";
  public static final String DB_ITEM_TYPE = "item_type";
  public static final String DB_RESOURCE_SERVER_URL = "resource_server_url";

  //table names
  public static final String POLICY_TABLE = "policy";
  public static final String USER_TABLE = "user_table";
  public static final String NOTIFICATION_TABLE = "request";
  public static final String APPROVED_ACCESS_REQUEST_TABLE = "approved_access_requests";
  public static final String RESOURCE_ENTITY_TABLE = "resource_entity";
  public static final List<String> POLICY_TABLE_COLUMNS =
      List.of(DB_CONSUMER_EMAIL, DB_ITEM_ID, DB_OWNER_ID, DB_STATUS, DB_EXPIRY_AT,
          DB_CONSTRAINTS);
  public static final List<String> USER_TABLE_COLUMNS =
      List.of(DB_ID, DB_EMAIL_ID, DB_FIRST_NAME, DB_LAST_NAME);
  public static final List<String> NOTIFICATION_TABLE_COLUMNS =
      List.of(DB_ID, DB_USER_ID, DB_ITEM_ID, DB_OWNER_ID, DB_STATUS, DB_EXPIRY_AT,
          DB_CONSTRAINTS, DB_ADDITIONAL_INFO);
  public static final List<String> APPROVED_ACCESS_REQUEST_TABLE_COLUMNS =
      List.of(DB_ID, DB_REQUEST_ID, DB_POLICY_ID);
  public static final List<String> RESOURCE_ENTITY_TABLE_COLUMNS =
      List.of(DB_ID, DB_PROVIDER_ID, DB_RESOURCE_GROUP_ID, DB_ITEM_TYPE, DB_RESOURCE_SERVER_URL);

}
