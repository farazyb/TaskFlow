CREATE INDEX idx_users_email       ON users (email);
CREATE INDEX idx_tasks_status      ON tasks (status);
CREATE INDEX idx_tasks_assignee_id ON tasks (assignee_id);
CREATE INDEX idx_tasks_creator_id  ON tasks (creator_id);
