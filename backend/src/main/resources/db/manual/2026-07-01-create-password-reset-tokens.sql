create table if not exists password_reset_tokens (
    id bigserial primary key,
    utilisateur_id bigint not null references utilisateurs(id),
    token_hash varchar(64) not null unique,
    expires_at timestamp not null,
    used boolean not null default false,
    created_at timestamp not null,
    used_at timestamp
);

create index if not exists idx_password_reset_tokens_utilisateur_used
    on password_reset_tokens(utilisateur_id, used);
