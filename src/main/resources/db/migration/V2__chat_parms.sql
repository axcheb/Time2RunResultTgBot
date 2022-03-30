create table "chat_params"
(
    "chat_id"           bigint primary key,
    "lost_positions"    jsonb,
    "updated_at"        date not null
);

CREATE UNIQUE INDEX IF NOT EXISTS `index_chat_params_chat_id` ON "chat_params" ("chat_id");