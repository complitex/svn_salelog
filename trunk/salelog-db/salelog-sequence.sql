update sequence set sequence_value = (select IFNULL(max(`object_id`), 0) from `order`)+1 where sequence_name = 'order';
