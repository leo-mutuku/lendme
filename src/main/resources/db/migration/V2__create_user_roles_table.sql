CREATE TABLE user_roles (
                            user_id UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                            role    VARCHAR(20) NOT NULL,

                            PRIMARY KEY (user_id, role),
                            CONSTRAINT ck_user_roles_role CHECK (role IN ('USER', 'ADMIN'))
);

-- Older versions of V1 had a single "role" column on users.
-- Roles now live in user_roles, so remove it if it exists.
ALTER TABLE users DROP COLUMN IF EXISTS role;