
COPY users ( user_id , password , first_name , last_name , e_mail , street1 , state , country , zipcode , balance)
FROM '/tmp/wyong001/dataset/users.data' 
WITH DELIMITER ';';
 
COPY super_user ( super_user_id )
FROM '/tmp/wyong001/dataset/superusers.data' 
WITH DELIMITER ';';

COPY follow (user_id_to , user_id_from , follow_time)
FROM '/tmp/wyong001/dataset/followers.data' 
WITH DELIMITER ';';

COPY video ( video_id , title , year, online_price, dvd_price)
FROM '/tmp/wyong001/dataset/video.data' 
WITH DELIMITER ';';

COPY genre ( genre_id , genre_name )
FROM '/tmp/wyong001/dataset/genre.data' 
WITH DELIMITER ';';

COPY categorize ( video_id , genre_id )
FROM '/tmp/wyong001/dataset/categorize.data' 
WITH DELIMITER ';';

COPY director ( director_id , first_name , last_name )
FROM '/tmp/wyong001/dataset/director.data' 
WITH DELIMITER ';';

COPY star ( star_id , first_name , last_name )
FROM '/tmp/wyong001/dataset/stars.data' 
WITH DELIMITER ';';

COPY author ( author_id , first_name , last_name )
FROM '/tmp/wyong001/dataset/authors.data' 
WITH DELIMITER ';';

COPY directed ( video_id , director_id)
FROM '/tmp/wyong001/dataset/directed.data' 
WITH DELIMITER ';';

COPY played ( video_id, star_id)
FROM '/tmp/wyong001/dataset/played.data' 
WITH DELIMITER ';';

COPY written ( video_id , author_id )
FROM '/tmp/wyong001/dataset/written.data' 
WITH DELIMITER ';';


COPY series ( series_id , title)
FROM '/tmp/wyong001/dataset/series.data' 
WITH DELIMITER ';';

COPY season ( season_id , series_id , season_number)
FROM '/tmp/wyong001/dataset/season.data' 
WITH DELIMITER ';';

COPY comment (comment_id , user_id ,video_id , comment_time , content)
FROM '/tmp/wyong001/dataset/comment.data'
WITH DELIMITER ';';
