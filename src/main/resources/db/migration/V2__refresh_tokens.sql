CREATE TABLE refresh_tokens (
    id         BIGSERIAL   PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL,
    user_id    BIGINT      NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked    BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_refresh_tokens_hash  UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Lookup by user (e.g. revoke-all-for-user on password change)
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
-- Cleanup scan: expired or revoked tokens
CREATE INDEX idx_refresh_tokens_expires_revoked ON refresh_tokens (expires_at, revoked);
