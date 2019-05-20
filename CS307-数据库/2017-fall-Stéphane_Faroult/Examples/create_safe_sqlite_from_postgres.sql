--
-- Queries table description from Postgres
-- to recreate a SQLite table with constraints (INCOMPLETE)
--
\t on
select case ordinal_position
         when 1 then 'create table ' || table_name
                     || ' ('
         else '            '
       end
       || column_name || ' ' || data_type ||
       case
         when data_type like 'character%'
              then '(' || cast(character_maximum_length
                               as varchar) || ')'
         else ''
       end || ' '
          || case
              when data_type like 'character%'
                  then 'constraint ' 
                       || column_name || '_char'
                     || ' check(length(' || column_name
                     || ')<=' || cast(character_maximum_length as varchar)
                     || ')'
              when data_type like 'integer%'
                  then 'constraint ' 
                       || column_name || '_num'
                     || ' check(' || column_name
                     || '+0=' || column_name || ')'
              when data_type like 'timestamp%'
                   or data_type like 'date%'
                  then 'constraint ' 
                       || column_name || '_date'
                     || ' check(datetime(datetime('
                            || column_name
                     || ',''+1 day''),''-1 day'') is not null)'
               else ''
              end 
       || case ordinal_position
            when cnt then ');'
            else ','
       end    
from (
select table_name,
       column_name,
       ordinal_position,
       data_type,
       character_maximum_length,
       count(*) over (partition by table_name) cnt
from information_schema.columns
where table_schema = current_schema) x
order by table_name, ordinal_position
;
