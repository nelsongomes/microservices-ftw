package com.msftw.api.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.msftw.db.DatabaseHelper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.glassfish.jersey.server.ManagedAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Api
@ApiModel(value = "APIv1", description = "Microservices FTW Methods")
@Path("/v1")
public class ApiV1 {
  private static HazelcastInstance clusterNode = Hazelcast.newHazelcastInstance(new Config());
  private static ObjectMapper mapper = new ObjectMapper();
  private static final Logger log = LoggerFactory.getLogger(ApiV1.class);
  private static IAtomicLong counter = clusterNode.getAtomicLong("counter");

  public static String V1_DATA_SOURCE = "config/hikari.properties";

  @SuppressWarnings("static-method")
  @GET
  @ManagedAsync
  @Path("/ping")
  @Produces(MediaType.TEXT_PLAIN)
  @ApiOperation(value = "Ping method", notes = "Method to check API connectivity.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "successful operation", response = String.class) })
  public void pingMethod(@Context final HttpServletRequest request,
      @Suspended final AsyncResponse asyncResponse) {
    log.info("Received request for {} from IP {}", request.getRequestURI(),
        request.getRemoteAddr());
    counter.incrementAndGet();

    asyncResponse.resume("pong");
  }

  /**
   * Method to get all books from database.
   *
   * @return a List of books.
   */
  @SuppressWarnings("static-method")
  @GET
  @ManagedAsync
  @Path("/books")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "List Books method", notes = "Method to get a books list.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "successful operation", response = Book.class,
          responseContainer = "List"),
      @ApiResponse(code = 500, message = "unsuccessful operation", response = String.class) })
  public void getBooks(@Suspended final AsyncResponse asyncResponse) {
    counter.incrementAndGet();
    final QueryRunner runner = new QueryRunner(DatabaseHelper.getDataSource(V1_DATA_SOURCE));

    try {
      asyncResponse
      .resume(
          Response.ok()
          .entity(mapper.writeValueAsString(
              runner.query("select * from books", new BeanListHandler<>(Book.class))))
          .build());
      return;
    } catch (final SQLException e) {
      asyncResponse.resume(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN)
          .entity("Internal server error: " + e.getMessage()).build());
      return;
    } catch (final JsonProcessingException e) {
      asyncResponse.resume(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN)
          .entity("Internal server error: " + e.getMessage()).build());
      return;
    }
  }

  /**
   * Method to query a Hazelcast cluster for total request count.
   *
   * @return total requests
   */
  @SuppressWarnings("static-method")
  @GET
  @ManagedAsync
  @Path("/counter")
  @Produces(MediaType.TEXT_PLAIN)
  @ApiOperation(value = "Get your cluster request counter",
  notes = "Method to get a cluster counter.")
  @ApiResponses(
      value = { @ApiResponse(code = 200, message = "successful operation", response = Long.class) })
  public void getCounter(@Suspended final AsyncResponse asyncResponse) {
    asyncResponse.resume(new Long(counter.get()));
  }

  /**
   * Method to create an array of book instances.
   *
   * @param books
   *          list of books
   * @param comment
   *          a sample book
   * @return a boolean response.
   */
  @POST
  @ManagedAsync
  @Path("/books")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Create Books method", notes = "Method to create one or more books.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "successful operation", response = Boolean.class,
          responseContainer = "List"),
      @ApiResponse(code = 500, message = "unsuccessful operation", response = String.class,
      responseHeaders = {@ResponseHeader(name="X-Host", description="hostname that failed")}) })
  public void createBooks(
      @ApiParam(value = "Json array of Books", required = true) final LinkedList<Book> books,
      @ApiParam(value = "sample comment",
          required = false) @QueryParam("comment") final String comment,
      @Suspended final AsyncResponse asyncResponse) {
    counter.incrementAndGet();
    if (books != null && books.size() > 0) {
      final QueryRunner runner = new QueryRunner(DatabaseHelper.getDataSource(V1_DATA_SOURCE));

      try {
        final int[] records = runner.batch(
            "insert into books (title, isbn, price, publisher) values (?,?,?,?)",
            this.prepareForInsert(books));

        // if sum all batchs matches
        if (books.size() == Arrays.stream(records).sum()) {
          // success
          asyncResponse.resume(Response.ok().entity(Boolean.TRUE).build());
          return;
        }
        asyncResponse.resume(Response.ok().entity(Boolean.FALSE).build());
        return;
      } catch (final SQLException e) {
        log.error("failed insert", e);
        asyncResponse.resume(Response.status(Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.TEXT_PLAIN).entity("Failed insert: " + e.getMessage()).build());
        return;
      }
    }

    asyncResponse.resume(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN)
        .entity("No books received!").build());
  }

  /**
   * Method to prepare data for insertion.
   *
   * @param books
   * @return an array of object arrays
   */
  @SuppressWarnings("static-method")
  private Object[][] prepareForInsert(final LinkedList<Book> books){
    final Object[][] params = new Object[books.size()][];

    for (int i = 0; i < books.size(); i++) {
      final Book book = books.get(i);
      params[i] = new Object[] { book.getTitle(), book.getIsbn(), book.getPrice(),
          book.getPublisher() };
    }
    return params;
  }
}
