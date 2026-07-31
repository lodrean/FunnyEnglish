-- Create student groups tables

-- Groups table
CREATE TABLE IF NOT EXISTS student_groups (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    teacher_id UUID NOT NULL,
    invite_code VARCHAR(20) UNIQUE NOT NULL,
    max_students INTEGER NOT NULL DEFAULT 30,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Group members table
CREATE TABLE IF NOT EXISTS group_members (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL,
    user_id UUID NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(group_id, user_id)
);

-- Join requests table
CREATE TABLE IF NOT EXISTS group_join_requests (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL,
    user_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    processed_by UUID,
    UNIQUE(group_id, user_id, status)
);

-- Foreign keys
ALTER TABLE student_groups 
    ADD CONSTRAINT fk_groups_teacher 
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE group_members 
    ADD CONSTRAINT fk_members_group 
    FOREIGN KEY (group_id) REFERENCES student_groups(id) ON DELETE CASCADE;

ALTER TABLE group_members 
    ADD CONSTRAINT fk_members_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE group_join_requests 
    ADD CONSTRAINT fk_requests_group 
    FOREIGN KEY (group_id) REFERENCES student_groups(id) ON DELETE CASCADE;

ALTER TABLE group_join_requests 
    ADD CONSTRAINT fk_requests_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Indexes
CREATE INDEX idx_groups_teacher ON student_groups(teacher_id);
CREATE INDEX idx_groups_invite_code ON student_groups(invite_code);
CREATE INDEX idx_members_group ON group_members(group_id);
CREATE INDEX idx_members_user ON group_members(user_id);
CREATE INDEX idx_requests_group ON group_join_requests(group_id);
CREATE INDEX idx_requests_user ON group_join_requests(user_id);
CREATE INDEX idx_requests_status ON group_join_requests(status);
