create table author
(
    id          serial primary key,
    name         text not null,
    created_at  timestamp default current_timestamp
);

alter table budget add column author_id integer references author(id) on delete set null;