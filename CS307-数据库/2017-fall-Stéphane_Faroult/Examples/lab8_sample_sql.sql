--
--  A procedure to register a film
--
create or replace function movie_registration
           (p_title        varchar,
            p_country_name varchar,
            p_year         int,
            p_director_fn  varchar,
            p_director_sn  varchar,
            p_actor1_fn    varchar,
            p_actor1_sn    varchar,
            p_actor2_fn    varchar,
            p_actor2_sn    varchar)
returns void
as $$
declare
  n_rowcount  int;
  n_movieid   int;
  n_people    int;
begin
  insert into movies(title, country, year_released)
  select p_title, country_code, p_year
  from countries
  where country_name = p_country_name;
  get diagnostics n_rowcount = ROW_COUNT;
  if n_rowcount = 0
  then
    raise exception 'country not found in table COUNTRIES';
  end if;
  n_movieid := lastval();
  --
  -- Count how many people were "provided" in the parameters
  -- (some of them could be NULL)
  --
  select count(surname)
  into n_people
  from (select p_director_sn as surname
        union all
        select p_actor1_sn as surname
        union all
        select p_actor2_sn as surname) specified_people
  where surname is not null;
  --
  -- Get people identifiers and insert into table credits 
  --
  insert into credits(movieid, peopleid, credited_as)
  select n_movieid, people.peopleid, provided.credited_as
  from (select coalesce(p_director_fn, '*') as first_name,
               p_director_sn as surname,
               'D' as credited_as
        union all
        select coalesce(p_actor1_fn, '*') as first_name,
               p_actor1_sn as surname,
               'A' as credited_as
        union all
        select coalesce(p_actor2_fn, '*') as first_name,
               p_actor2_sn as surname,
               'A' as credited_as) provided
       inner join people
         on people.surname = provided.surname
        and coalesce(people.first_name, '*') = provided.first_name
  where provided.surname is not null;
  get diagnostics n_rowcount = ROW_COUNT;
  if n_rowcount != n_people
  then
    -- My choice is to cancel everything ("raise" will
    -- generate a rollback of the whole procedure, the
    -- successful insert into movies will be cancelled)
    raise exception 'Some people couldn''t be found';
  end if;
end;
$$ language plpgsql;
--
--  Same procedure, but also allows to add more people to 
--  an existing film. Check how the exception is caught when
--  the film is inserted
--
create or replace function movie_registration(p_title    varchar,
                                   p_country  varchar,
                                   p_year     int,
                                   p_director varchar,
                                   p_actor1   varchar,
                                   p_actor2   varchar)
returns void
as $$
declare
  n_rowcount int;
  n_movieid  int;
  n_people   int;
begin
  begin  -- Nested block to catch the exception
    insert into movies(title, country, year_released)
    select p_title, country_code, p_year
    from countries
    where country_name = p_country;
    get diagnostics n_rowcount = ROW_COUNT;
    if n_rowcount = 0
    then
      raise exception 'country not found in table COUNTRIES'; 
    end if;
    n_movieid := lastval();
  exception
    when unique_violation then -- Film already there
          select m.movieid
          into n_movieid
          from movies m
               inner join countries c
                  on c.country_code = m.country
          where m.title = p_title
            and m.year_released = p_year
            and c.country_name = p_country;
  end;
  --
  -- Count how many people are specified
  -- Remember that count(col) ignores
  -- columns that are null.
  --
  select count(name)
  into n_people
  from (select p_director as name
        union all
        select p_actor1 as name
        union all
        select p_actor2 as name) specified_people;
  --
  -- Get people identifiers and insert into table credits 
  --
  insert into credits(movieid, peopleid, credited_as)
  select n_movieid, people.peopleid, provided.credited_as
  from (select case position(',' in full_name)
                 when 0 then '*'
                 else trim(substr(full_name, 1 + position(',' in full_name)))
               end first_name,
               case position(',' in full_name)
                 when 0 then full_name
                 else trim(substr(full_name, 1, position(',' in full_name) - 1))
               end surname,
               credited_as  
        from (select p_director as full_name,
                     'D' as credited_as
              union all
              select p_actor1 as full_name,
                     'A' as credited_as
              union all
              select p_actor2 as full_name,
                     'A' as credited_as) q
        where full_name is not null) provided
       inner join people
         on people.surname = provided.surname
        and coalesce(people.first_name, '*') = provided.first_name;
  get diagnostics n_rowcount = ROW_COUNT;
  if n_rowcount != n_people 
  then
    raise exception 'Some people couldn''t be found'; 
  end if;
end;
$$ language plpgsql;
--
-- Just to demonstrate that strings can be split ...
-- It would be completely different SQL with another DBMS.
-- 
with str as (select cast('Curtiz,Michael;Flynn,Errol;de Havilland,Olivia;Rathbone,Basil;Rains,Claude' as varchar) list)
select split_part(full_name, ',', 1) as surname,
       split_part(full_name, ',', 2) as first_name
from (select split_part(list, ';', n) full_name
      from str
           cross join generate_series(1,20) n
               -- Assuming that there are at most 20 names in the list
      where n <= 1 + length(list) - length(replace(list, ';', ''))) x
               -- Counting names by counting separators 
;
--
--  An example where using a cursor is fully justified.
--  The procedure queries the catalogue to check the tables
--  in the current schema, and creates a copy (with the date in the
--  name) of every table for which no copy was created today and 
--  that isn't itself a copy.
--  There is no way to do it without a cursor.
--           
create or replace function save_tables()
returns void
as $$
declare
   v_suffix      varchar(50);
   v_create_cmd  varchar(100);
   c cursor for select replace(table_name, v_suffix, '') as table_name
                from information_schema.tables
                where table_schema = current_schema()
                group by replace(table_name, v_suffix, '')
                having count(*) = 1
                    and replace(table_name, v_suffix, '') not like '%_save_%';

begin
  select '_save_' || to_char(current_date, 'YYMMDD')
  into v_suffix;
  for fetched_row in c
  loop
    v_create_cmd := 'create table ' || fetched_row.table_name || v_suffix
                    || ' as select * from ' || fetched_row.table_name;
    execute v_create_cmd;
  end loop;
end;
$$ language plpgsql;
