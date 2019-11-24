create table hhuser(
     user_id SERIAL PRIMARY KEY,
     first_name VARCHAR(124),
     last_name VARCHAR(124)
);

create table resume(
     resume_id SERIAL PRIMARY KEY,
     user_id INTEGER,
     description VARCHAR(512),
     is_active BOOLEAN
);
