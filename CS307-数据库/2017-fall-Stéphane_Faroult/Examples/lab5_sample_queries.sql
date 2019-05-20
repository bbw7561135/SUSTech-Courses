--
-- Q1. Marilyn Monroe appeared in how many 1952 films?
--
--  Two search criteria, the name of a person, and the year of
--  of a film. We can throw all of it together in a 3-table
--  join and let the optimizer sort it out ...
--  All wrapped of course in a count(*)
--
select count(*)
from (select m.movieid
      from movies m  -- We need it because of the year
           join credits c
             on c.movieid = m.movieid
            and c.credited_as = 'A' -- because of "appeared"
                          -- She never directed a film but 
                          -- we might have the issue with
                          -- someone else
           join people p
             on p.peopleid = c.peopleid
      where p.first_name = 'Marilyn'
        and p.surname = 'Monroe'
        and m.year_released = 1952) x;
    -- The fact that movies appears first in the join
    -- doesn't mean that the SQL engine will first look
    -- for the 1952 films. In fact, it will probably start
    -- with table people, because it knows (we have said that
    -- the combination is unique) that one first_name and surname
    -- match 0 or 1 person. It may or not know (more about this
    -- later) how many 1952 films there are. Other considerations
    -- come into play, as we'll see during coming lectures.
--
-- Q2. Display first name, surname, year of death and year
--     of their last film for actors who died more than 20 years
--     after the last film we have with them in the database.
--
select p.first_name, p.surname, a.last_film, p.died
from (select c.peopleid, max(m.year_released) last_film
      from movies m
           join credits c
             on c.movieid = m.movieid
      where credited_as = 'A' -- Could as well go in the join
                              -- condition. I prefer putting
                              -- explicit criteria (the question
                              -- says "actors") in the where clause
      group by c.peopleid) a
     join people p  -- join AFTER the aggregate!
                    -- Fewer bytes to move around
       on p.peopleid = a.peopleid
where p.died > 20 + a.last_film
;
--
-- Q3. Films where you can find XXX and YYY playing together?
--
--     Very classic question - associations. Are two people
--     involved in the same committee and therefore know each
--     other? Do people who buy this article also often buy
--     this one? You can use whomever you want for XXX and YYY,
--     I have used one of the mythical Hollywood couples (now,
--     if you prefer superhero movies to classic cinema, up
--     to you to use other names ...)
--
select m.title, m.country, m.year_released
from (select c.movieid
      from -- Most peole, I think, would join
           -- table people twice, once for Bogart and
           -- once for Bacall. No need to.
           -- I'm probably scrapping a few microseconds
           -- by involving table people only once, which
           -- may become significant if the query is
           -- run (with parameters and changing names)
           -- very often.
           (select peopleid
                   -- Note that I don't care which peopleid
                   -- is Bogart and which peopleid is Bacall.
                   -- I just want the two together.
            from people
            where (first_name = 'Humphrey'
                   and surname = 'Bogart')
               or (first_name = 'Lauren'
                   and surname = 'Bacall')) famous_couple
           join credits c
             on c.peopleid = famous_couple.peopleid
            and c.credited_as = 'A' -- 'playing' means actors
      group by c.movieid  -- Note that I'm just grouping
                          -- short numerical identifiers,
                          -- not long titles. Titles will
                          -- come later.
      having count(*) = 2) bogart_plus_bacall
     join movies m  -- Now it's a piece of cake to retrieve
                    -- film information
       on m.movieid = bogart_plus_bacall.movieid
order by m.year_released -- make it chronological
;
--
-- Q3. How many times did John Wayne play in a John Ford
--     film in the database?
--
--     A variant of the preceding question, with a twist:
--     the two people are not having the same role, so knowing
--     who is whom becomes important.
--
select count(*)
from (select movieid
      from (select peopleid,
                   case surname
                     when 'Ford' then 'D'
                     else 'A'
                   end credited_as
            from people
            where first_name = 'John' -- Taking advantage of the
                                      -- fact that it's the same
                                      -- first name for typing less
              and surname in ('Wayne', 'Ford')) wayne_ford
           join credits c
             on c.peopleid = wayne_ford.peopleid
            and c.credited_as = wayne_ford.credited_as
      group by movieid
      having count(c.peopleid) = 2) by_ford_with_wayne
      -- There is no problem with Ford directing and playing
      -- or Wayne co-directing with Ford because we
      -- also join on credited_as. A laxer join
      -- might require either count(distinct c.peopleid) = 2
      -- or count(c.peopleid) >= 2.
      -- Lax joins are better avoided.
--
-- Q6. List the names of the known directors of 2015 films
--     (no need to display anything about the film). If the
--     film is Chinese, Korean or Japanese, the name should
--     be displayed as surname followed by first name, otherwise
--     it must be first name followed by surname.
--
select distinct   -- required, there might be two films for
                  -- one director
       case m.country
         when 'cn' then surname || ' ' || coalesce(first_name, ' ')
         when 'tw' then surname || ' ' || coalesce(first_name, ' ')
         when 'hk' then surname || ' ' || coalesce(first_name, ' ')
         when 'jp' then surname || ' ' || coalesce(first_name, ' ')
         when 'kr' then surname || ' ' || coalesce(first_name, ' ')
         else coalesce(first_name, ' ') || ' ' || surname
       end as director
from movies m
     join credits c
       on c.movieid = m.movieid
     join people p
       on p.peopleid = c.peopleid
where c.credited_as = 'D'
and m.year_released = 2015
;
--  Note that coalesce() is required in this version, otherwise
--  all the directors who are only known by one name (common
--  with directors from Southern India) have a null row returned
--  for them. If you want the result to look really nice, you
--  should add a trim() to remove the two spaces before (or 
--  after) the name of people who only have a surname.
--  It's also possible to have two separate case ... end returning
--  surname in one case, and first_name in the other, then the
--  reverse ... but then naming the columns becomes a bit
--  difficult (name1 and name2? That's ugly).
--
-- Q7. What are the title, country and year of release of remakes
--     in the database ? (films for which an earlier film with
--     the same title exists)
--
--     This is of course a very loose definition of a remake,
--     because sometimes films have the same title but they aren't
--     the same storyr; sometimes remakes have a different
--     title ("My Fair Lady" is a musical remake of a "Pygmalion"
--     done in the 1930s, which kept the same title as the
--     original George Bernard Shaw play), especially when
--     Americans do a remake of a non-American film ("Scent of a
--     Woman" vs "Profumo di Donna", which means the same thing
--     in italian). There are however many true remakes that fit
--     the question ...
--
--     There are different ways of answering the question.
--     One is to use an aggregate:
--     We can for instance find the first occurrence of each title
--     in the database for titles that appear several times:
select title, min(year_released) as first_one
from movies
group by title
having count(*) > 1
;
--
-- This result can be joined on the title, and we can return films
-- with a later year:
--
select m.title, m.country, m.year_released
from (select title,
             min(year_released) as first_one
      from movies
      group by title
      having count(*) > 1) x
     join movies m
       on m.title = x.title
where m.year_released > x.first_one
order by m.title,
         m.year_released 
;
--
-- Another option is to join table movies to itself.
--
select distinct r.title, r.country, r.year_released
from movies m  
     join movies r -- remakes
       on r.title = m.title
      and r.year_released > m.year_released
order by r.title,
         r.year_released
;
-- In that case, notice the DISTINCT. It's required, because
-- otherwise a second remake would appear twice, once because
-- it's more recent than the original film (OK) and a second time
-- because it's more recent than the first remake!
-- You might not notice it before you pay attention to films
-- called "The Three Musketeers", "Devdas", "King Kong" or
-- "Treasure Island".
-- No such problem in the first version of the query, because
-- the comparison is always with the oldest version.

