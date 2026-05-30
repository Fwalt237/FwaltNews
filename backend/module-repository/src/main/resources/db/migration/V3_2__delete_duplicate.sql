DELETE FROM news n1 USING news n2
WHERE n1.id > n2.id AND n1.title = n2.title;

ALTER TABLE news DROP CONSTRAINT IF EXISTS unique_news_title;
ALTER TABLE news ADD CONSTRAINT unique_news_title UNIQUE (title);

DELETE FROM authors a USING authors b WHERE a.id > b.id AND a.name = b.name;
ALTER TABLE authors DROP CONSTRAINT IF EXISTS unique_author_name;
ALTER TABLE authors ADD CONSTRAINT unique_author_name UNIQUE (name);

DELETE FROM tags t1 USING tags t2 WHERE t1.id > t2.id AND t1.name = t2.name;
ALTER TABLE tags DROP CONSTRAINT IF EXISTS unique_tag_name;
ALTER TABLE tags ADD CONSTRAINT unique_tag_name UNIQUE (name);