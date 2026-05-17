CREATE TABLE IF NOT EXISTS users (
    id       BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    CONSTRAINT uq_users_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS positions (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT           NOT NULL,
    ticker       VARCHAR(20)      NOT NULL,
    quantity     DOUBLE PRECISION NOT NULL DEFAULT 0,
    avg_cost     DOUBLE PRECISION NOT NULL DEFAULT 0,
    target_price DOUBLE PRECISION,
    stop_price   DOUBLE PRECISION,
    CONSTRAINT fk_positions_user        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_positions_user_ticker UNIQUE (user_id, ticker)
);

CREATE TABLE IF NOT EXISTS watchlist_items (
    id      BIGSERIAL   PRIMARY KEY,
    user_id BIGINT      NOT NULL,
    ticker  VARCHAR(20) NOT NULL,
    CONSTRAINT fk_watchlist_user        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_watchlist_user_ticker UNIQUE (user_id, ticker)
);