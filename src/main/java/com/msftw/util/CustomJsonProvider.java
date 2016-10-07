package com.msftw.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import javax.ws.rs.ext.Provider;

@Provider
public class CustomJsonProvider extends JacksonJaxbJsonProvider {
  private static ObjectMapper mapper = new ObjectMapper();

  static {
    CustomJsonProvider.mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,
        true);
  }

  public CustomJsonProvider() {
    super();
    this.setMapper(CustomJsonProvider.mapper);
  }
}
