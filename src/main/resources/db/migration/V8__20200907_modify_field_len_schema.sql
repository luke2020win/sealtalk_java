
/*修改 扩展 friendships表中的字段长度*/
ALTER TABLE friendships MODIFY COLUMN displayName varchar(512);
ALTER TABLE friendships MODIFY COLUMN message varchar(256);
ALTER TABLE friendships MODIFY COLUMN description varchar(1024);

/*修改 扩展 groups表中的字段长度*/
ALTER TABLE groups MODIFY COLUMN name varchar(64);

/*修改 扩展 groups表中的字段长度*/
ALTER TABLE group_members MODIFY COLUMN displayName varchar(128);
ALTER TABLE group_members MODIFY COLUMN groupNickname varchar(128);
ALTER TABLE group_members MODIFY COLUMN memberDesc varchar(1024);
