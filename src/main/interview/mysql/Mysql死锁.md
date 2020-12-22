
update learn.inventory 	set lock_qty = 2.46 where id = 314044536575331328
and server_update_time = ?

分析： id是趋势递增的，会不会有间隙锁，导致了某一个区域锁住，从而形成了死锁？