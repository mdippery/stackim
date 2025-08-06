CREATE TABLE tags (
    id         serial                      PRIMARY KEY,
    name       varchar(256)                NOT NULL UNIQUE,
    stack_id   integer                     NOT NULL,
    created_at timestamp with time zone    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE hits (
    id        serial                       PRIMARY KEY,
    tag_id    integer                      REFERENCES tags (id),
    timestamp timestamp with time zone     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    referer   varchar(1024)                NOT NULL
);
