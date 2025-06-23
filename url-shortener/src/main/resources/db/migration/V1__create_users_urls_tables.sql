DROP SEQUENCE IF EXISTS urls_seq;

CREATE SEQUENCE urls_seq
    START WITH 3844
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE urls
(
    id           BIGINT        NOT NULL DEFAULT nextval('urls_seq'),
    short_code   VARCHAR(8)    NOT NULL,
    original_url VARCHAR(2048) NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    clicks       BIGINT        NOT NULL DEFAULT 0,
    user_id      UUID,
    CONSTRAINT pk_urls PRIMARY KEY (id)
);

CREATE TABLE users
(
    id         UUID         NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    name       VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE urls
    ADD CONSTRAINT uc_urls_shortcode UNIQUE (short_code);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE urls
    ADD CONSTRAINT FK_URLS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);