INSERT INTO users(username,email,password,first_name,last_name,enabled)
VALUES('admin','admin@example.com','$2a$10$y5EkrL.Q045nu8.2REASJeK2NwEuiw5pCKmKD2UZNR1iE/4Dv8WH2',
       'Admin','User',TRUE);

INSERT INTO user_roles(user_id,role) VALUES (1,'ROLE_ADMIN');

INSERT INTO users(username,email,password,first_name,last_name,enabled)
VALUES('user','user@example.com','$2a$10$5LnjHIejutMeTTZNrTZpFe8I5icRbDqkGFktw.7bUKdlwzwufIYlC',
       'Normal','User',TRUE);

INSERT INTO user_roles(user_id,role) VALUES (1,'ROLE_USER');