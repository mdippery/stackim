CREATE TABLE tags (
    id         serial                      PRIMARY KEY,
    name       varchar(256)                NOT NULL UNIQUE,
    stack_id   integer                     NOT NULL,
    created_at timestamp with time zone    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX tags_name_idx ON tags (name);

CREATE TABLE hits (
    id        serial                       PRIMARY KEY,
    tag_id    integer                      REFERENCES tags (id),
    timestamp timestamp with time zone     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    referer   varchar(1024)                NOT NULL
);
CREATE INDEX hits_tag_id_idx ON hits (tag_id);
CREATE INDEX hits_timestamp_idx ON hits (timestamp);
