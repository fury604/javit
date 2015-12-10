create table if not exists alt_binaries_test_yenc (
	id bigint PRIMARY KEY,
	subject text,
	frm varchar(200),
	post_date date,
	status varchar(30),
	msgid varchar(200),
	bytes varchar(15),
	parts longtext,
	total_parts int,
	server varchar(50),
	port int,
	server_group varchar(50)
);
