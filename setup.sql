CREATE ROLE stackim
WITH
  LOGIN PASSWORD 'password'
  CREATEDB
;

CREATE DATABASE stackim
WITH
  OWNER stackim
;

CREATE TABLE tags (
    id         serial       PRIMARY KEY,
    name       varchar(256) NOT NULL UNIQUE,
    stack_id   integer      NOT NULL,
    created_at timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE hits (
    id        serial        PRIMARY KEY,
    tag_id    integer       REFERENCES tags (id),
    timestamp timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    referer   varchar(1024) NOT NULL
);
