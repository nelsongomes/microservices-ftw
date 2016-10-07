package com.msftw.api.v1;

import io.swagger.annotations.ApiModelProperty;

public class Book {
  @ApiModelProperty(value = "Book id", required = false, example = "1")
  private Integer id;
  @ApiModelProperty(value = "Book title", required = true, example = "Book Title")
  private String title;
  @ApiModelProperty(value = "Book ISBN", required = true, example = "Book ISNTitle")
  private String isbn;
  @ApiModelProperty(value = "Book Price", required = true, example = "12.12")
  private Float price;
  @ApiModelProperty(value = "Publisher Name", required = true, example = "Publisher")
  private String publisher;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public Float getPrice() {
    return price;
  }

  public void setPrice(Float price) {
    this.price = price;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }
}
