package iudx.apd.acl.server.authentication.util;

public class Constants {
  public static final String SEARCH_PATH = "/user/search";
  public static final String AUTH_CERTIFICATE_PATH = "/cert";
  public static final String AUTH_JWKS_PATH = "/jwks";
  public static final String USER_ID = "userId";
  public static final String IS_DELEGATE = "isDelegate";
  public static final String ROLE = "role";
  public static final String AUD = "aud";
  /*user's information is updated as soon as the user uses any provider or consumer respective API*/
  public static final String INSERT_USER_TABLE = "insert into user_table(_id,email_id,first_name,last_name) " +
      " values ($1,$2,$3,$4)" +
      " on conflict (_id) do update SET (email_id,first_name,last_name) " +
      "= (EXCLUDED.email_id,EXCLUDED.first_name,EXCLUDED.last_name) RETURNING _id";
  public static final String GET_USER = "select * from user_table where _id=$1::UUID;";
  public static final int JWT_LEEWAY_TIME = 30;
}
