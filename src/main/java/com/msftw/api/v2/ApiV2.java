package com.msftw.api.v2;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/v2")
public class ApiV2 {

  @Path("/ping")
  @GET
  public String ping() {
    return "pong2";
  }
}
