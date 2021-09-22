package org.learn.algorithm;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @description:
 * @create: 2020-11-05 23:04
 */
public class LRU实现 {
    private class Entry<K, V> {
        private Entry<K, V> pre;
        private Entry<K, V> next;
        private K key;
        private V value;
    }

    public class LRUCache<K, V> {
        private final int MAX_CACHE_SIZE;
        private Entry<K, V> first;
        private Entry<K, V> last;

        private HashMap<K, Entry<K, V>> hashMap;

        public LRUCache(int size) {
            MAX_CACHE_SIZE = (int) (Math.ceil(size / 0.75) + 1);

            // LinkedHashMap 实现
            LinkedHashMap<String, String> hashMap = new LinkedHashMap(MAX_CACHE_SIZE, 0.75f, true) {
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            };
            hashMap.put("", "");

            LRUCache.this.hashMap = new HashMap<>(MAX_CACHE_SIZE, 0.75f);
        }

        //https://www.cnblogs.com/lzrabbit/p/3734850.html
        public void put(K key, V value) {
            Entry<K, V> entry = hashMap.get(key);
            if (entry == null) {
                if (hashMap.size() >= MAX_CACHE_SIZE) {
                    //todo
                }

                entry = new Entry<>();
                entry.key = key;
                entry.value = value;
                hashMap.put(key, entry);

                moveToFirst(entry);
            } else {
                entry.value = value;
                moveToFirst(entry);
            }
        }

        private void moveToFirst(Entry<K, V> entry) {
            if (first == entry) {
                // 修改头结点[first]的值
                return;
            }

            //当前节点更新，且在中间位置，[1,2,3,  4, 5] ==> 4，移动到1，
            if (entry.next != null) {
                entry.pre.next = entry.next;

                if (entry.pre != null) {
                    entry.next.pre = entry.pre;
                }
            }
            if (entry.pre != null) {
                entry.next.pre = entry.pre;
            }

            if (first == null) {
                first = entry;
            } else {
                //注意顺序，将entry插入到first前面
                entry.next = first;
                first.pre = entry;

                first = entry;
                entry.pre = null;
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            Entry entry = first;
            while (entry != null) {
                sb.append(String.format("%s:%s ", entry.key, entry.value));
                entry = entry.next;
            }
            return sb.toString();
        }
    }


}
