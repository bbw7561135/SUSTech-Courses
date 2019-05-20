drop table if exists json_test;
create table json_test(id serial,
                       json_string json);
insert into json_test(json_string)
values('{"surname":"Doe","first_name":"John","born":1970}');
insert into json_test(json_string)
values('{"first_name":["John","Jane"], "born":[1970, 1975]}');
select cast(json_string as varchar) from json_test;
select cast(json_string->'born' as varchar) from json_test;
select cast(json_string->'surname' as varchar) from json_test;
select cast(json_string->'first_name' as varchar) from json_test;
-- json_each_text also requires a cast
select id, cast(json_each(json_string) as varchar) from json_test;
select id, json_object_keys(json_string) from json_test;
