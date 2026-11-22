-- TRUE when the token was exchanged at /refresh. Only reuse of a rotated token
-- signals theft; tokens revoked by logout or password change are simply rejected.
ALTER TABLE refresh_tokens ADD COLUMN rotated BOOLEAN NOT NULL DEFAULT FALSE;
