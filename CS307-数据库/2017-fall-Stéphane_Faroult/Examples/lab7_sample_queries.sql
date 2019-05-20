--
-- Country/territory with the most films per continent
-- and per year since 2000
-- There may be ties (OCEANIA, typically), the next
-- query solves ties
select continent,
       year_released,
       country_name as top_producing_country
from (select c.continent,
             m.year_released,
             c.country_name,
             rank() over (partition by c.continent,
                                       m.year_released
                          order by m.films desc) rnk 
      from (select country,
                   year_released,
                   count(*) films
            from movies
            where year_released >= 2000
            group by country,
                     year_released) m
           join countries c
             on c.country_code = m.country) x
where x.rnk = 1
order by continent, year_released -- or anything else ...
;
--
--   Introducing string_agg().
--   This type of function exist with a different name
--   in every dialect of SQL, sometimes you can specify
--   the delimiter, sometimes it's a mandatory comma.
--   MySQL (that has no window function) accepts an ORDER BY
--   in its own function (called GROUP_CONCAT).
--   Usually, the order is the underlying order of the rows
--   ... which is mostly random after a few operations.
--   The query after this one shows how with PostgreSQL
--   keep the ties in alphabetical order.
--
select continent,
       year_released,
       string_agg(country_name, ',') as top_producing_country
from (select c.continent,
             m.year_released,
             c.country_name,
             rank() over (partition by c.continent,
                                       m.year_released
                          order by m.films desc) rnk 
      from (select country,
                   year_released,
                   count(*) films
            from movies
            where year_released >= 2000
            group by country,
                     year_released) m
           join countries c
             on c.country_code = m.country) x
where x.rnk = 1
group by continent, year_released
order by continent, year_released -- or anything else ...
;
-- Ordering ties
-- You must order before calling string_add() if you want
-- to sort say by country name in the aggregate.
select continent,
       year_released,
       string_agg(country_name, ',') as top_producing_country
from (select c.continent,
             m.year_released,
             c.country_name,
             rank() over (partition by c.continent,
                                       m.year_released
                          order by m.films desc) rnk 
      from (select country,
                   year_released,
                   count(*) films
            from movies
            where year_released >= 2000
            group by country,
                     year_released) m
           join countries c
             on c.country_code = m.country
       -- The ORDER BY is here. It doesn't affect ranking
       -- (although obviously it's more efficient, like here,
       -- to have ordering that doesn't conflict)
       order by c.continent, m.year_released, c.country_name) x
where x.rnk = 1
group by continent, year_released
order by continent, year_released -- or anything else ...
;
-- 
-- The same string_agg() is particularly useful for
-- showing all the film information on one line.
-- Here all Chinese films since 2010, including the
-- title in Chinese characters (traditional Chinese
-- for HK and TW, I've tried to keep to the "original
-- title" - there may be traditional+simplified for
-- co-productions), and the international english title.
-- Several things to notice: like all group functions, string_agg()
-- ignore nulls (and can be used as a window function).
-- Multiple aggregates (on different sources) are usually tricky.
-- I managed to aggregate directors and actors in a single pass because
-- the information comes from the same tables, and I could use case to
-- create two columns out of one. The alternate titles come from another
-- table, and joining everything together would have created many duplicate
-- rows that would have been hard to manage. I have chosen here to nest
-- aggregates.
-- Another solution follows.
-- 
select a.year_released,
       a.title,
       string_agg(b.title, ',') also_known_as,
       a.directors,
       a.actors
from (select m.movieid,
             m.year_released,
             m.title,
             string_agg(trim(case c.credited_as
                               when 'D' then trim(p.surname
                                      || ' ' || coalesce(p.first_name))
                                -- Trim eliminates the space when 
                                -- there is no first name (eg Angelababy)
                               else null
                             end), ',') as directors,
             string_agg(trim(case c.credited_as
                              when 'A' then trim(p.surname
                                      || ' ' || coalesce(p.first_name))
                              else null
                            end), ',') as actors
      from movies m
           join credits c
             on c.movieid = m.movieid
           join people p
             on p.peopleid = c.peopleid
      where m.country in ('cn', 'hk', 'tw')
        and m.year_released >= 2010
      group by m.movieid, m.year_released, m.title) a
    join alt_titles b
      on b.movieid = a.movieid
group by a.year_released,
         a.title,
         a.directors,
         a.actors
order by a.year_released, a.title
;
--
-- Same result (with one space after commas), different writing.
--
with chinese_films as 
        (select movieid, title, year_released
         from movies
         where country in ('cn', 'hk', 'tw')
           and year_released >= 2010)
select b.year_released, 
       b.title,
       b.also_known_as,
       a.directors,
       a.actors
from (select cf.movieid,
             string_agg(case c.credited_as
                          when 'D' then trim(p.surname || ' ' ||
                                             coalesce(p.first_name, ''))
                          else null
                        end, ', ') directors,
             string_agg(case c.credited_as
                          when 'A' then trim(p.surname || ' ' ||
                                             coalesce(p.first_name, ''))
                          else null
                        end, ', ') actors
     from chinese_films cf
          join credits c
            on c.movieid = cf.movieid
          join people p
           on p.peopleid = c.peopleid
     group by cf.movieid) a
    join (select cf.movieid, cf.title, cf.year_released,
                 string_agg(at.title, ', ') also_known_as
          from chinese_films cf
               join alt_titles at
                 on at.movieid = cf.movieid
          group by cf.movieid, cf.title, cf.year_released) b
     on b.movieid = a.movieid
order by b.year_released, b.title 
;
--
-- Other aggregate functions that weren't mentioned
-- during the lectures are interesting too.
-- For instance, let's count the number of films
-- per year and country since 2000:
--
select year_released, country, count(*) as films
from movies
where year_released >= 2000
group by year_released, country
;
--
-- We can with ntile() assign each of this result to
-- a "bucket" (tǒng), let's say that we want 5 buckets
-- per year. We assign a country to a bucket based on
-- the number of films produced. The top 20% of producers
-- will therefore be in bucket 1, the bottom 20% in bucket
-- 5.
--
select year_released, country, films,
       ntile(5) over (partition by year_released
                      order by films desc) as bucket
from (select year_released, country, count(*) as films
      from movies
      where year_released >= 2000
      group by year_released, country) x
;
--
-- We can use this (and string_agg()) to list the top 20%
-- producing countries per year, by decreasing number of
-- films in the string_agg() order.
--
select year_released,
       string_agg(country_name, ',') as top_20_pct
from (select z.year_released, c.country_name
      from (select year_released,
                   country,
                   films
            from (select year_released, country, films,
                         ntile(5) over (partition by year_released
                                        order by films desc) as bucket
                  from (select year_released, country, count(*) as films
                        from movies
                        where year_released >= 2000
                        group by year_released, country) x) y
            where bucket = 1) z
           join countries c
             on c.country_code = z.country
      order by z.year_released, films desc) w -- for ordering string_agg
group by year_released
order by year_released
;
-- 
-- Other interesting window functions are lag() and lead().
-- They allow you to compare the current row to a previous (usually THE
-- previous) row for lag() and to a following (usually next) row within
-- the same category for lead()
--
-- For instance we can compute the number of US films per year since
-- 2000:
--
select year_released, count(*) as films
from movies
where country = 'us'
  and year_released >= 2000
group by year_released
order by year_released
;
--
-- Then we can return the previous value next to the current one:
--
select year_released,
       films,
       lag(films, 1) over (order by year_released) as previous
from (select year_released, count(*) as films
      from movies
      where country = 'us'
        and year_released >= 2000
      group by year_released) a
order by year_released
;
--
-- Then we can compute the year-on-year change as a percentage
--
select year_released,
       films,
       round(100 * (films - previous) / previous) as pct_change
from (select year_released,
             films,
             lag(films, 1) over (order by year_released) as previous
      from (select year_released, count(*) as films
            from movies
            where country = 'us'
            and year_released >= 2000
            group by year_released) a) b
order by year_released
;
--
-- The alternate way of writing it (with SQLite for instance) is the following
-- one with a self join (note that it's a self left-join, because
-- we don't return the number of films for 1999) which is a bit less
-- efficient:
--
with us_prod as (select year_released, count(*) films
                 from movies
                 where country = 'us'
                   and year_released >= 2000
                 group by year_released) 
select c.year_released, 
       c.films,
       round(100 * (c.films - p.films) / p.films) as pct_change
from us_prod c -- current
     left outer join us_prod p -- previous
       on p.year_released = c.year_released - 1
order by c.year_released
--
-- *****  Functions *****
--
-- Although it's possible to run SQL queries in functions,
-- they SHOULD NOT execute queries. It's not their purpose.
--
-- The proper case when to use a function is when it replaces
-- a complicated scalar expression, as in the following example.
--
-- Function that shows a number of minutes as "hours:minutes" string
--
-- PostgreSQL support function overloading (same name, different parameters)
-- so you must specify the parameters when dropping the function.
-- Not the case with Oracle.
--
drop function show_minutes_as_hour(minutes int)
;
create function show_minutes_as_hour(minutes int)
returns varchar
as $$
-- declare  -- variables could declared here if needed
--   varname varchar(50);
begin
   -- use cast() and to_char() (which allows formatting) to return
   -- numbers as strings. With to_char format '99' would return
   -- 5 as <space>5, format '00' returns it as '05'
   -- I'm using trim() because otherwise there is a space for the sign
   -- (+ is 'space', or -)
   return cast(trunc(minutes/60) as varchar) || ':'
             || trim(to_char(minutes % 60, '00'));
   -- Please refer to the PostgreSQL docs, for instance
   --   https://www.postgresql.org/docs/9.6/static/functions-formatting.html
   -- for specific details about string formatting.
end;
$$ language plpgsql;
-- Note that if you want to create the function under Squirrel SQL,
-- you must go to Sessions->Session Properties
-- then SQL tab and change the Statement Separator from ';' to something else
-- (for intance //). Otherwise Squirrel SQL sends one piece to the server
-- that stops at the first encountered ';', and the server cannot make
-- sense of it. With the separator changed as suggested, you type everything
-- as above and end with
--     ...
--  end;
--  $$ language plpgsql
--  //
--
--  You can then restore the default separator, or use the new one for 
--  all queries ...
-- 
--
-- Test
select departure, arrival, duration, show_minutes_as_hour(duration)
from flights
order by random()
limit 10
;
