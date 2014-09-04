drop table comment CASCADE;
CREATE TABLE comment (
	comment_id SERIAL PRIMARY KEY,
	user_id VARCHAR(9) NOT NULL,
	video_id INTEGER NOT NULL,
	comment_time TIMESTAMP NOT NULL,
	content VARCHAR(300) NOT NULL,
	FOREIGN KEY(user_id) REFERENCES users on delete cascade,
	FOREIGN KEY(video_id) REFERENCES video on delete cascade
);




drop table users CASCADE;
CREATE TABLE users (
	user_id VARCHAR(9) NOT NULL PRIMARY KEY,
	password VARCHAR(36) NOT NULL,
	first_name VARCHAR(40) NOT NULL, 
	middle_name VARCHAR(40),
	last_name VARCHAR(40) NOT NULL,
	e_mail VARCHAR(40) NOT NULL,
	street1 VARCHAR(40),
	street2 VARCHAR(40),
	state VARCHAR(10),
	country VARCHAR(20),
	zipcode VARCHAR(20),
	balance INTEGER NOT NULL
);

drop table super_user CASCADE;
CREATE TABLE super_user (
	super_user_id VARCHAR(9) NOT NULL PRIMARY KEY,
	FOREIGN KEY(super_user_id) REFERENCES users
	on delete cascade
);

drop table follow CASCADE;
CREATE TABLE follow (
	user_id_to VARCHAR(9) NOT NULL ,
	user_id_from VARCHAR(9) NOT NULL, 
	follow_time TIMESTAMP NOT NULL,
	PRIMARY KEY(user_id_to,user_id_from),
	FOREIGN KEY(user_id_to) REFERENCES users on delete cascade,
	FOREIGN KEY(user_id_from) REFERENCES users on delete cascade
);

drop table series CASCADE;
CREATE TABLE series (
	series_id  SERIAL PRIMARY KEY,
	title  VARCHAR(50) NOT NULL
);

drop table season CASCADE;
CREATE TABLE season (
	season_id SERIAL PRIMARY KEY,
	series_id  INTEGER NOT NULL,
	season_number INTEGER NOT NULL,
	FOREIGN KEY(series_id) REFERENCES series
	on delete cascade
);
drop table video CASCADE;
CREATE TABLE video (
	video_id SERIAL PRIMARY KEY,
	title VARCHAR(50) NOT NULL,
	year INTEGER NOT NULL,
	online_price INTEGER NOT NULL,
	dvd_price INTEGER NOT NULL,
	votes INTEGER,
	rating INTEGER,
	episode VARCHAR(9) ,
	season_id INTEGER ,
	FOREIGN KEY(season_id) REFERENCES season
	on delete cascade
);
drop table genre CASCADE;
CREATE TABLE genre (
	genre_id SERIAL PRIMARY KEY,
	genre_name VARCHAR(50) NOT NULL
);
drop table prefers CASCADE;
CREATE TABLE prefers (
	user_id VARCHAR(9) NOT NULL,
	genre_id INTEGER NOT NULL,
	PRIMARY KEY(user_id, genre_id),
	FOREIGN KEY(user_id) REFERENCES users on delete cascade,
	FOREIGN KEY(genre_id) REFERENCES genre on delete cascade
);

drop table categorize CASCADE;
CREATE TABLE categorize (
	video_id INTEGER  NOT NULL,
	genre_id INTEGER  NOT NULL,
	PRIMARY KEY(video_id, genre_id),
	FOREIGN KEY(video_id) REFERENCES video on delete cascade,
	FOREIGN KEY(genre_id) REFERENCES genre on delete cascade
);

drop table director CASCADE;
CREATE TABLE director (
	director_id SERIAL PRIMARY KEY,
	first_name VARCHAR(40) NOT NULL,
	last_name VARCHAR(40) NOT NULL
);

drop table star CASCADE;
CREATE TABLE star (
	star_id SERIAL PRIMARY KEY,
	first_name VARCHAR(40) NOT NULL,
	last_name VARCHAR(40) NOT NULL
);

drop table author CASCADE;
CREATE TABLE author (
	author_id SERIAL PRIMARY KEY,
	first_name VARCHAR(40) NOT NULL,
	last_name VARCHAR(40) NOT NULL
);
drop table directed CASCADE;
CREATE TABLE directed (
	video_id INTEGER NOT NULL,
	director_id INTEGER NOT NULL,
	PRIMARY KEY(video_id, director_id),
	FOREIGN KEY(video_id) REFERENCES video on delete cascade,
	FOREIGN KEY(director_id) REFERENCES director on delete cascade
);
drop table played CASCADE;
CREATE TABLE played (
	video_id INTEGER NOT NULL,
	star_id INTEGER NOT NULL,
	PRIMARY KEY(video_id, star_id),
	FOREIGN KEY(video_id) REFERENCES video on delete cascade,
	FOREIGN KEY(star_id) REFERENCES star on delete cascade
);
drop table written CASCADE;
CREATE TABLE written (
	video_id INTEGER NOT NULL,
	author_id INTEGER NOT NULL,
	PRIMARY KEY(video_id, author_id),
	FOREIGN KEY(video_id) REFERENCES video on delete cascade,
	FOREIGN KEY(author_id) REFERENCES author on delete cascade
);

drop table likes CASCADE;
CREATE TABLE likes (
	user_id VARCHAR(9) NOT NULL,
	video_id INTEGER NOT NULL,
	PRIMARY KEY(user_id, video_id),
	FOREIGN KEY(video_id) REFERENCES video on delete cascade,
	FOREIGN KEY(user_id) REFERENCES users on delete cascade
);
drop table orders CASCADE;
CREATE TABLE orders (
	order_id SERIAL PRIMARY KEY,
	video_id INTEGER NOT NULL,
	user_id VARCHAR(9) NOT NULL,
	FOREIGN KEY(video_id) REFERENCES video on delete cascade,
	FOREIGN KEY(user_id) REFERENCES users on delete cascade
);
drop table rate CASCADE;
CREATE TABLE rate (
	user_id VARCHAR(9) NOT NULL,
	video_id INTEGER NOT NULL,
	rate_time TIMESTAMP,
	rating INTEGER,
	PRIMARY KEY (user_id, video_id),
	FOREIGN KEY(user_id) REFERENCES users on delete cascade,
	FOREIGN KEY(video_id) REFERENCES video
	on delete cascade
);
