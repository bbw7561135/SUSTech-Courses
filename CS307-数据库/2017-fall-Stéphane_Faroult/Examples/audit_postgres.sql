--
--  This is the code for the audit trigger shown during
--  the lecture, with one row per column modified.
--
--  Note: if you want to audit changes on several tables,
--        you could store the audit trail in a single table if:
--          * you add a column with the table name
--          * every audited table has a single-column numerical PK that you
--            could store in a column called "id" instead of "peopleid".
--       If the second condition isn't satisfied, you could imagine
--       converting everything to varchar and possibly concatenating 
--       multiple columns (that would be required for credits, as the PK
--       is made of all three columns), but it becomes murky.
--
drop trigger people_trg on people;
drop table people_audit;
create table people_audit(auditid         serial,
                          peopleid        int,
                          type_of_change  char(1),
                          column_name     varchar(30),
                          old_value       varchar(250),
                          new_value       varchar(250),
                          changed_by      varchar(100),
                          time_changed    timestamp);
create or replace function people_audit_fn()
returns trigger
as
$$
begin
  if tg_op = 'UPDATE'
  then
    insert into people_audit(peopleid,
                             type_of_change,
                             column_name,
                             old_value,
                             new_value,
                             changed_by,
                             time_changed)
    select peopleid, 'U', column_name, old_value, new_value,
           current_user||'@'
               || coalesce(cast(inet_client_addr() as varchar),
                                'localhost'),
           current_timestamp
    from (select old.peopleid,
                 'first_name'   column_name,
                 old.first_name old_value,
                 new.first_name new_value
          where coalesce(old.first_name, '*') <> coalesce(new.first_name, '*')
          union all
          select old.peopleid,
                 'surname'   column_name,
                 old.surname old_value,
                 new.surname new_value
          where old.surname <> new.surname
          union all
          select old.peopleid,
                 'born'   column_name,
                 cast(old.born as varchar) old_value,
                 cast(new.born as varchar) new_value
          where old.born <> new.born
          union all
          select old.peopleid,
                 'died'   column_name,
                 cast(old.died as varchar) old_value,
                 cast(new.died as varchar) new_value
          where coalesce(old.died, -1) <> coalesce(new.died, -1)) modified;
  elsif tg_op = 'INSERT' then
    insert into people_audit(peopleid,
                             type_of_change,
                             column_name,
                             new_value,
                             changed_by,
                             time_changed)
    select peopleid, 'I', column_name, new_value,
           current_user||'@'
               || coalesce(cast(inet_client_addr() as varchar),
                                'localhost'),
           current_timestamp
    from (select new.peopleid,
                 'first_name'   column_name,
                 new.first_name new_value
          where new.first_name is not null
          union all
          select new.peopleid,
                 'surname'   column_name,
                 new.surname new_value
          union all
          select new.peopleid,
                 'born'   column_name,
                 cast(new.born  as varchar) new_value
          union all
          select new.peopleid,
                 'died'   column_name,
                 cast(new.died  as varchar) new_value
          where new.died is not null) inserted;
  else  
    insert into people_audit(peopleid,
                             type_of_change,
                             column_name,
                             old_value,
                             changed_by,
                             time_changed)
    select peopleid, 'D', column_name, old_value,
           current_user||'@'
               || coalesce(cast(inet_client_addr() as varchar),
                                'localhost'),
           current_timestamp
    from (select old.peopleid,
                 'first_name'   column_name,
                 old.first_name old_value
          where old.first_name is not null
          union all
          select old.peopleid,
                 'surname'   column_name,
                 old.surname old_value
          union all
          select old.peopleid,
                 'born'   column_name,
                 cast(old.born  as varchar) old_value
          union all
          select old.peopleid,
                 'died'   column_name,
                 cast(old.died  as varchar) old_value
          where old.died is not null) deleted;
  end if;
  return null;
end;
$$ language plpgsql;
create trigger people_trg
after insert or update or delete on people
for each row
execute procedure people_audit_fn();
--
insert into people(first_name, surname, born)
values('Ryan', 'Gosling', 1980);
insert into people(first_name, surname, born)
values('George', 'Clooney', 1961);
insert into people(first_name, surname, born)
values('Frank', 'Capra', 1897);
update people
set died = 1991
where first_name = 'Frank'
  and surname = 'Capra';
delete from people
where first_name = 'Ryan'
  and surname = 'Gosling';
select * from people_audit;
