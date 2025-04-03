package org.cdpg.dx.common.models;


import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;
import java.util.Objects;

@DataObject
@JsonGen(publicConverter = false)
public class Response {
  public  String type;
  public  String title;
  public  String detail;
  public int statusCode;

  public Response(){}

  public Response(Response other)
  {
    this.type = other.getType();
    this.detail = other.getDetail();
    this.title = other.getTitle();
    this.statusCode = other.getStatusCode();
  }
  public Response(JsonObject jsonObject){
    ResponseConverter.fromJson(jsonObject, this);
  }

  /**
   * Converts Data object or User class object to json object [Serialization]
   *
   * @return JsonObject
   */
    public JsonObject toJson() {
      JsonObject jsonObject = new JsonObject();
      ResponseConverter.toJson(this, jsonObject);
      return jsonObject;
    }

  public String getType() {
    return type;
  }

  public Response setType(String type) {
    this.type = type;
    return this;
  }

  public String getTitle() {
    return title;
  }

  public Response setTitle(String title) {
    this.title = title;
    return this;
  }

  public String getDetail() {
    return detail;
  }

  public Response setDetail(String detail) {
    this.detail = detail;
    return this;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public Response setStatusCode(int statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Response response)) return false;
    return statusCode == response.statusCode && Objects.equals(type, response.type) &&
        Objects.equals(title, response.title) && Objects.equals(detail, response.detail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, title, detail, statusCode);
  }
}
