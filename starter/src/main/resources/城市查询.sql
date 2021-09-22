-- 查看mysql8的当前隔离级别
select @@global.transaction_isolation,@@transaction_isolation;


-- learn.city表    省 索引 shop_city_id
-- 解读，直接使用 中文查询就好，不必还转成一个id，设计成parentId之类。

-- 省 + 市 查询
SELECT * FROM learn.city
where country_code = 'CN'
  and shop_city_id > -1
order by ownership1, ownership2

-- 查询县
SELECT * FROM learn.city
where country_code = 'CN'
  and ownership2 = '成都'