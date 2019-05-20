--
-- Reminder: Window functions are available in Oracle,
--           SQL Server, DB2, PostgreSQL, but not in
--           MySQL, MariaDB or SQLite (at the time
--           of writing)
--
-- Q1. Three most recent films per country?
--
--     Because the database stores the year of release without
--     any detail about the precise date, it's impossible
--     to make a difference between say two 2016 films.
--
--     The query requires ranking years. Because of ties, if we
--     have 5 films for 2017 we should all return them.
--     Any 2016 film will be ranked 6th or more with the rank()
--     function, which is or isn't good (depends on what you
--     really want ...), and 2nd with dense_rank(). I have chosen
--     to use dense_rank(), but this is debatable. I'm really
--     returning the three most recent years for which I have
--     films.
--
select c.country_name as country,
       x.year_released,
       x.title
from (select m.country, m.year_released, m.title,
             dense_rank() over (partition by m.country
                                order by m.year_released desc) rnk
      from movies m) x
     join countries c
       on c.country_code = x.country
where rnk <= 3
order by c.country_name, x.rnk, x.title
;
-- Without the window function, you need a subquery that counts
-- for each film either how many films or how many years are
-- more recent. Window functions are far more efficient.
--
-- Q2. Since 2000, percentage of films from each country
--     every year?
--
--     Computing percentages is an area where Window functions
--     shine. Without them, you can use factorizing (common table
--     expressions), unless of course you are on MySQL.
--
--     We are combining here a regular aggregate (how many
--     films per country and year?) performed, as usual,
--     before any join that isn't necessary.
--     You should also notice that the condition on the year
--     is added BEFORE the aggregate, to avoid aggregating
--     every year for every country in the database before
--     discarding one century of cinema.
--     The world-wide production of films is computed by 
--     summing up the number of films per year over all 
--     countries.
--
select c.country_name as country, y.year_released, y.pct
from (select x.country, x.year_released,
             round(100 * films /
                 sum(x.films)
                      over (partition by x.year_released), 1) pct
      from (select m.country, m.year_released, count(*) as films
            from movies m
            where m.year_released >= 2000
            group by m.country, m.year_released) x) y
     join countries c
       on c.country_code = y.country
order by y.year_released, y.pct desc
;
--
--   Bonus: You can also see over the years which percentage
--   of the films comes from a given country. You can filter
--   on the country name at the outer level; however, it would
--   be more efficient once again to filter before the aggregate,
--   in which case I would push the join on countries inside the
--   subquery named x, and return (and group by) c.country_name
--   instead of m.country. The country name will be costlier to
--   process than the code, but all in all the ability to filter
--   before the aggregate should be beneficial.
--   If you want to see decline, witness Italy between the 1950s
--   and now ... France (where cinema is heavily subsidized by
--   the state) and, to a lesser extent, UK have managed to keep
--   at the same level. Indian and Chinese films are getting
--   increasingly more successful internationally (also true
--   of Mexico or Brazil).
--  
