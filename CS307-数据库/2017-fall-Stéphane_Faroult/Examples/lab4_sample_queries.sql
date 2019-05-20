--
--  These are possible answers to some of the Lab 4 questions,
--  with some discussion
--
-- Q1. What is the most common surname in the database, and how
--     many time does it appear?
--
--  The best possible answer in my view uses window functions, which
--  exist in Oracle, SQL Server, PostgreSQL and DB2 but unfortunately
--  not in MySQL or SQLite
--
select surname,
       name_count
from (select surname,
             name_count,
             rank()
                over (order by name_count desc) rnk
      from (select surname, count(*) as name_count
            from people
            group by surname) names ) ranking
where rnk = 1
;
--
-- You might be tempted by the following solution, which has
-- a major flaw: it cannot handle ties (you would have the same 
-- problem using row_number() instead of rank() in the previous
-- query) (doesn't work with Oracle or DB2, who use a different
-- syntax). So, it will give the correct result, but there is no
-- guarantee that it will ALWAYS give the correct result.
--
select surname, count(*) as name_count
from people                                         
group by surname
order by name_count desc
limit 1
;
--
--  The "standard" way (far less efficient) that you would find
--  as "the correct answer" in countless books and on countless
--  websites is the following one, that handles ties correctly.
--
select surname, count(*) as name_count
from people
group by surname
having count(*) =
     (select max(name_count) max_count
      from (select surname, count(*) as name_count
      from people
      group by surname) names)
;
--
-- You can improve performance (30% better on PostgreSQL) by
-- using what is known as "query factorization", called in 
-- the SQL Server world "Common Table Expression" and uses
-- basically the same syntax (but much simpler) as recursive
-- queries. It's supported by every DBMS except MySQL (MariaDB,
-- a MySQL clone created by the original creator of MySQL, has
-- added it recently)
--
with names as (select surname,
                      count(*) as name_count
               from people
               group by surname)
select *
from names
where name_count =
       (select max(name_count) max_count
        from names)
;
--
-- With MySQL there are some options other than the standard one
-- but they are ugly, only work on MySQL and aren't significantly
-- faster than the standard answer, at least on this example.
--
-- Q2. What are the films directed by John Woo?
--
--  The following query can be written as a straight join
--  between people, credits and movies, with the conditions
--  on first_name and surname in the (common) where clause.
--  I have written a subquery to retrieve the peopleid to
--  emphasize how it will be processed.
select m.*
from (select peopleid
      from people
      where first_name = 'John'
        and surname = 'Woo') john_woo
     join credits c
       on c.peopleid = john_woo.peopleid
      and c.credited_as = 'D'
     join movies m
       on m.movieid = c.movieid
order by m.year_released
;
--
-- The following queries return exactly the same result. If you have
-- a decent query optimizer, all queries will end up executing the
-- same thing and will run in the same time. But it's not always the
-- case: on MySQL there is a noticeable difference, especially on the
-- first run (things tend to even out when data is loaded in memory
-- and queries executed multiple times)
-- Overall there may not be a big difference between a query that runs
-- in 0.01 sec and one that runs in 0.07 sec, but repeat the query 10,000
-- times and it becomes another story.
--
-- Generally speaking, avoid nested subqueries with multiple "exists"
--
select m.*
from  movies m
where exists (select null from credits c
              where c.movieid = m.movieid
                and c.credited_as = 'D'
                and exists (select null
                            from people p
                            where p.peopleid = c.peopleid
                              and p.first_name = 'John'
                              and p.surname = 'Woo'))
order by m.year_released
;
--
--  This is far more decent.
--
select m.*
from  movies m
where m.movieid in (select c. movieid
                    from credits c
                         join people p
                           on p.peopleid = c.peopleid
                    where c.credited_as = 'D'
                      and p.first_name = 'John'
                      and p.surname = 'Woo')
order by m.year_released
;
--
-- Q3. Directors who have directed films in more than one country.
--
-- I also return the number of countries, which I think is useful
-- information. Otherwise, I could NOT have count(distinct m.country)
-- in the select of the query called "international_directors" and
-- only have it in the having clause (in the same way that "where"
-- can be applied to a column you don't show, "having" can be applied
-- to an aggregate you don't show).
-- Notice that "distinct" is REQUIRED into the count, otherwise any
-- director having directed more than one film would qualify, which
-- would of course be wrong.
--
-- If I want to return the count (or order by the count), I need a join.
-- I could also join, then apply the count, but don't forget that any
-- group by means sorting data. Here I'm operating against numerical
-- identifiers and 2-letter country codes. Grouping with names would
-- mean far more data to sort.
-- 
select p.first_name, p.surname,
       international_directors.different_countries
from (select c.peopleid,
             count(distinct m.country) different_countries 
      from credits c
           join movies m
             on m.movieid = c.movieid
      where c.credited_as = 'D'
      group by c.peopleid
      having count(distinct m.country) > 1) international_directors
     join people p
       on p.peopleid = international_directors.peopleid
order by international_directors.different_countries,
         p.surname,
         p.first_name
;
--
-- Q4. Case of the American 1950 "Treasure Island", with easy-to-read
--     labels instead of 'A' and 'D'
--
--  Code translation means either a join when you have a reference table
--  (country code translated to country name) or a case ... end when you
--  have none.
--  I have tried to anticipate on what would happen if one day I get
--  something else than actors or directors in my credits table (writers
--  or producers, for intance). I could restrict credited_as to 'A' and 'D'
--  with a condition. Or, what I have done here, I return the untranslated
--  code if I meet something unexpected.
--  Otherwise it's a fairly straight join.
--
select case c.credited_as
         when 'D' then 'Director'
         when 'A' then 'Actor'
         else c.credited_as
       end as cast_function,
       p.first_name,
       p.surname
from movies m
     join credits c
       on c.movieid = m.movieid
     join people p
       on p.peopleid = c.peopleid
where m.country = 'us'
  and m.title = 'Treasure Island'
  and m.year_released = 1950
order by case c.credited_as
           when 'D' then 1
           when 'A' then 2
           else 3
         end,
         p.surname
;
