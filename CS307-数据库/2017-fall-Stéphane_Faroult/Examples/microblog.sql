--
--  This example shows how to play with views and privileges.
--  All users write short messages into a table (thoughts) that 
--  they cannot access directly.
--  They are accessing it through views created in the 'public' schema
--  (beware: you cannot do it on the cs307 database because you haven't 
--  the privileges to create anything in 'public'; but you can change the
--  schema name and do it inside your own schema).
--  View my_thoughts only shows the curennt user's posts, and the author 
--  can do whatever s/he wants: create new posts, edit or delete previous
--  posts.
--  View microblog is select-only, and shows the current user's posts plus
--  all the posts of people "followed" by the current user.
--  You become a follower of someone by inserting that someone's user name
--  into view following, which maps to a hidden table called followers in
--  which the current user name is inserted by default.
--
--  You can start by playing with what is available in the database, and
--  create this in your schema with you wish.
--  Beware that with Postgres granting select on a table isn't enough,
--  you must also GRANT USAGE on your schema.
--
drop view if exists public.following;
drop view if exists public.my_thoughts;
drop view if exists public.microblog;
drop table if exists thoughts;
drop table if exists followers;
create sequence thought_id;
-- The following GRANT is required because when people insert
-- a new row they get the next value from the sequence.
grant usage on thought_id to public;
create table thoughts(id       int primary key
                               default nextval('thought_id'),
                      thought  varchar(200) not null,
                      author   varchar(50) not null
                               default current_user,
                      posted   timestamp
                               default current_timestamp);
create index thoughts_idx on thoughts(author);
create table followers(username  varchar(50) not null default current_user,
                       follows   varchar(50) not null,
                       constraint followers_pk
                                  primary key (username, follows));
create view public.microblog
as select author, thought, posted
from thoughts
where author = current_user
   or author in (select follows
                 from followers
                 where username = current_user)
;
grant select on public.microblog to public;
create view public.my_thoughts
as select id, thought, posted
   from thoughts
   where author = current_user;
grant select, insert, update, delete on public.my_thoughts to public; 
create view public.following
as select follows as username
   from followers
   where username = current_user;
grant select, insert, delete on public.following to public; 

                        
