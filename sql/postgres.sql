CREATE DATABASE msftw
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'C'
       LC_CTYPE = 'C'
       CONNECTION LIMIT = -1;

CREATE SEQUENCE public.books_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 5
  CACHE 1;
ALTER TABLE public.books_id_seq
  OWNER TO postgres;
  
CREATE TABLE public.books
(
  id integer NOT NULL DEFAULT nextval('books_id_seq'::regclass),
  title character varying(100),
  isbn character varying(20),
  price numeric(10,2),
  publisher character varying(100),
  CONSTRAINT pk_books PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.books
  OWNER TO postgres;
