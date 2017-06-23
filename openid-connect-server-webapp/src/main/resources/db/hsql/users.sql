--
-- Turn off autocommit and start a transaction so that we can use the temp tables
--

SET AUTOCOMMIT FALSE;

START TRANSACTION;

--
-- Insert user information into the temporary tables. To add users to the HSQL database, edit things here.
-- 

INSERT INTO users_TEMP (username, password, enabled) VALUES
  ('admin', 'password', TRUE),
  ('user', 'password', TRUE);


INSERT INTO authorities_TEMP (username, authority) VALUES
  ('admin', 'ROLE_ADMIN'),
  ('admin', 'ROLE_USER'),
  ('user', 'ROLE_USER');

-- By default, the username column here has to match the username column in the users table, above
-- INSERT INTO user_info_TEMP (sub, preferred_username, name, email, email_verified) VALUES
--   ('90342.ASDFJWFA','admin','Demo Admin','admin@example.com', true),
--   ('01921.FLANRJQW','user','Demo User','user@example.com', true);


-- By default, the username column here has to match the username column in the users table, above
INSERT INTO user_info_TEMP (sub, login, fio) VALUES
  ('90342.ASDFJWFA', 'adminUU', 'Иванов Админ Петрович'),
  ('90345.rfa', 'user', 'Береза Игорь Михайлович');

--
-- Merge the temporary users safely into the database. This is a two-step process to keep users from being created on every startup with a persistent store.
--

MERGE INTO users
USING (SELECT
         username,
         password,
         enabled
       FROM users_TEMP) AS vals(username, password, enabled)
ON vals.username = users.username
WHEN NOT MATCHED THEN
INSERT (username, password, enabled) VALUES (vals.username, vals.password, vals.enabled);

MERGE INTO authorities
USING (SELECT
         username,
         authority
       FROM authorities_TEMP) AS vals(username, authority)
ON vals.username = authorities.username AND vals.authority = authorities.authority
WHEN NOT MATCHED THEN
INSERT (username, authority) VALUES (vals.username, vals.authority);

-- MERGE INTO user_info
--   USING (SELECT sub, preferred_username, name, email, email_verified FROM user_info_TEMP) AS vals(sub, preferred_username, name, email, email_verified)
--   ON vals.preferred_username = user_info.preferred_username
--   WHEN NOT MATCHED THEN
--     INSERT (sub, preferred_username, name, email, email_verified) VALUES (vals.sub, vals.preferred_username, vals.name, vals.email, vals.email_verified);


MERGE INTO user_info
USING (SELECT
         sub,
         login,
         fio from user_info_TEMP) AS vals(sub, login, fio)
ON vals.login = user_info.login
WHEN NOT MATCHED THEN
INSERT (sub, login, fio) VALUES (sub, login, fio);


COMMIT;

SET AUTOCOMMIT TRUE;

