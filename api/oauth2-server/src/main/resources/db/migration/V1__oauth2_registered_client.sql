CREATE SCHEMA IF NOT EXISTS oauth2;

CREATE TABLE IF NOT EXISTS oauth2.oauth2_registered_client (
  id VARCHAR(255) NOT NULL,
  client_id VARCHAR(256) NOT NULL,
  client_id_issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  client_secret VARCHAR(256),
  client_secret_expires_at TIMESTAMP,
  client_name VARCHAR(256) NOT NULL,
  client_authentication_methods VARCHAR(1000) NOT NULL,
  authorization_grant_types VARCHAR(1000) NOT NULL,
  redirect_uris VARCHAR(1000) NOT NULL,
  post_logout_redirect_uris VARCHAR(1000),
  scopes VARCHAR(1000) NOT NULL,
  client_settings VARCHAR(2000) NOT NULL,
  token_settings VARCHAR(2000) NOT NULL,
  PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS oauth2_registered_client_client_id_idx ON oauth2.oauth2_registered_client (client_id);
