DELETE FROM authors a USING authors b
WHERE a.id > b.id AND a.name = b.name;

ALTER TABLE authors ADD CONSTRAINT unique_author_name UNIQUE (name);

DELETE FROM tags t1 USING tags t2
WHERE t1.id > t2.id AND t1.name = t2.name;

ALTER TABLE tags ADD CONSTRAINT unique_tag_name UNIQUE (name);

ALTER TABLE news ADD CONSTRAINT unique_news_title UNIQUE (title);

CREATE TABLE shedlock (
  name VARCHAR(64) NOT NULL,
  lock_until TIMESTAMP NOT NULL,
  locked_at TIMESTAMP NOT NULL,
  locked_by VARCHAR(255) NOT NULL,
  PRIMARY KEY (name)
);