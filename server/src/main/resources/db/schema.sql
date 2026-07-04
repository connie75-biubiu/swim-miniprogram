-- psql -U football -d swim -f schema.sql

CREATE TABLE t_user (
  id BIGSERIAL PRIMARY KEY,
  openid VARCHAR(64) NOT NULL UNIQUE,
  phone VARCHAR(11),
  nickname VARCHAR(50),
  avatar VARCHAR(255),
  role VARCHAR(10),
  gender SMALLINT,
  birth_month VARCHAR(7),
  stroke_order VARCHAR(100) DEFAULT '["自由泳","蛙泳","仰泳","蝶泳"]',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE workout (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  "date" DATE NOT NULL,
  stroke VARCHAR(20) NOT NULL,
  pool_type SMALLINT NOT NULL,
  source_type SMALLINT NOT NULL DEFAULT 1,
  total_distance INT NOT NULL,
  total_duration INT NOT NULL,
  avg_pace DECIMAL(6,2),
  note VARCHAR(200),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_workout_user_date ON workout (user_id, "date");
CREATE INDEX idx_workout_user_source ON workout (user_id, source_type, stroke);

CREATE TABLE split (
  id BIGSERIAL PRIMARY KEY,
  workout_id BIGINT NOT NULL,
  seq INT NOT NULL,
  stroke VARCHAR(20) NOT NULL,
  distance INT NOT NULL,
  duration INT NOT NULL,
  pace DECIMAL(6,2)
);

CREATE INDEX idx_split_workout_seq ON split (workout_id, seq);

CREATE TABLE coach_authorization (
  id BIGSERIAL PRIMARY KEY,
  student_id BIGINT NOT NULL,
  coach_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (student_id, coach_id)
);

CREATE INDEX idx_coach_auth_coach ON coach_authorization (coach_id);
