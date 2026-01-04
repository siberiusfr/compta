-- Fix default user passwords with correct BCrypt hashes
-- admin password: admin123
-- user password: user123

-- Update admin password (BCrypt hash for 'admin123')
UPDATE oauth2.users
SET password = '$2a$10$62MWZPPdej.YryISUfp6/elD4aegdQgNbi4I6dzIiuu5u/gjAvL..'
WHERE username = 'admin';

-- Update user password (BCrypt hash for 'user123')
UPDATE oauth2.users
SET password = '$2a$10$5caxZOj/Au6RLxjsltBqr.1MFh4S91m4f0qnvrT3U9wE3D37I0MCi'
WHERE username = 'user';
