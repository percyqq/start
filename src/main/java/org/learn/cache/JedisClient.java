package org.learn.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.params.SetParams;

import javax.annotation.Resource;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Jedis 客户端
 *
 * https://blog.csdn.net/zhulier1124/article/details/82193182
 */
public class JedisClient {

    private static final Logger logger = LoggerFactory.getLogger(JedisClient.class);

    //@Resource
    public JedisPool jedisPool;

    /**
     * 释放连接
     *
     * @param jedis
     * @param jedisPool
     * @param isBroken
     */
    public void release(Jedis jedis, JedisPool jedisPool, boolean isBroken) {
        if (jedis != null) {
            if (isBroken) {
                //jedisPool.returnBrokenResource(jedis);
            } else {
                //jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * 序列化
     *
     * @param object
     * @return
     * @author
     * @2015年3月9日
     */
    public static byte[] serialize(Object object) {
        ObjectOutputStream objectOutputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("serialize failed");
        } finally {
            try {
                if (objectOutputStream != null) {
                    objectOutputStream.close();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
            } catch (IOException e) {
                logger.warn("serialize failed");
            }
        }
        return null;
    }

    /**
     * 反序列化
     *
     * @param bytes
     * @return
     * @author
     * @2015年3月9日
     */
    public static Object deserialize(byte[] bytes) {
        try {
            return new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
        } catch (Exception e) {
            logger.warn("deserialize failed");
        }
        return null;
    }

    /**
     * 存储obj
     *
     * @param key
     * @param value
     * @param expireTime
     */
    public void setObj(String key, Object value, int expireTime) {
        Jedis jedis = null;
        boolean isBroken = false;
        try {
            jedis = jedisPool.getResource();
            if (expireTime == 0) {// 永不过期
                jedis.set(key.getBytes(), serialize(value));
            } else {
                jedis.setex(key.getBytes(), expireTime, serialize(value));
            }
        } catch (Exception e) {
            logger.warn("failed : jedis set obj key : " + key);
            isBroken = true;
        } finally {
            release(jedis, jedisPool, isBroken);
        }
    }

    /**
     * 批量获取obj
     *
     * @param keys
     * @return
     */
//    public <T> List<T> batchGet(List<String> keys, Class<T> clazz) {
//        try (Jedis jedis = jedisPool.getResource()) {
//            List<String> values = jedis.mget(keys.toArray(new String[keys.size()]));
//            values.removeAll(Collections.singleton(null));
//            if (CollectionUtils.isNotEmpty(values)) {
//                List<T> list = new ArrayList<>();
//                for (String value : values) {
//                    list.add(gsonSerializer.deserialize(value, clazz));
//                }
//                return list;
//            }
//        }
//        return null;
//    }

    /**
     * 批量获取obj
     *
     * @param keys
     * @return
     */
    public <T> List<T> batchGet(List<String> keys, Type typeoff) {
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> values = jedis.mget(keys.toArray(new String[keys.size()]));
            values.removeAll(Collections.singleton(null));
            if (!CollectionUtils.isEmpty(values)) {
                List<T> list = new ArrayList<>();
                for (String value : values) {
                    //list.add(gsonSerializer.deserialize(value, typeoff));
                }
                return list;
            }
        }
        return null;
    }

    /**
     * 批量设置obj
     *
     * @param keyObjs
     * @return
     */
    public <T> void batchSet(Map<String, T> keyObjs, int expireTime) {
        Map<String, String> keyValues = new HashMap<>();
        for (String key : keyObjs.keySet()) {
            T t = keyObjs.get(key);
//            String data = gsonSerializer.serialize(t);
//            keyValues.put(key, data);
        }
        try (Jedis jedis = jedisPool.getResource()) {
//            String result = Joiner.on(Constants.CACHE_VALUE_SPLIT).withKeyValueSeparator(Constants.CACHE_VALUE_SPLIT).join(keyValues);
//            jedis.mset(result.split(Constants.CACHE_VALUE_SPLIT));
            for (String key : keyValues.keySet()) {
                jedis.expire(key, expireTime);
            }
        }
    }

    /**
     * 获取obj
     *
     * @param key
     * @return
     */
    public Object getObj(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return deserialize(jedis.get(key.getBytes()));
        }
    }

    /**
     * 删除
     *
     * @param key
     */
    public void delObj(String key) {
        Jedis jedis = null;
        boolean isBroken = false;
        try {
            jedis = jedisPool.getResource();
            jedis.del(key.getBytes());
        } catch (Exception e) {
            logger.warn("failed : jedis delete obj key : " + key);
            isBroken = true;
        } finally {
            release(jedis, jedisPool, isBroken);
        }
    }

    /**
     * 模糊删除
     *
     * @param regex
     * @author
     * @2015年3月9日
     */
    public void fuzzyDel(String regex) {
        Jedis jedis = null;
        boolean isBroken = false;
        try {
            jedis = jedisPool.getResource();
            Set<String> keys = jedis.keys(regex);
            for (String key : keys) {
                del(key);
            }
        } catch (Exception e) {
            logger.warn("failed : jedis fuzzyDel key : " + regex);
            isBroken = true;
        } finally {
            release(jedis, jedisPool, isBroken);
        }
    }

    /**
     * 保存HashMap
     *
     * @param key
     * @param value
     * @param expireTime
     * @author
     * @2015年3月9日
     */
    public void hmset(String key, Map<String, String> value, int expireTime) {
        Jedis jedis = null;
        boolean isBroken = false;
        try {
            jedis = jedisPool.getResource();
            jedis.hmset(key, value);
            if (expireTime != 0) {
                jedis.expire(key, expireTime);
            }
        } catch (Exception e) {
            logger.warn("failed : jedis hmset key : " + key + " , value :" + value + " , expireTime :" + expireTime);
            isBroken = true;
        } finally {
            release(jedis, jedisPool, isBroken);
        }
    }

    /**
     * 获取hash
     *
     * @param key
     * @param fields
     * @return
     */
    public List<String> hmget(String key, String... fields) {
        Jedis jedis = null;
        boolean isBroken = false;
        List<String> list = null;
        try {
            jedis = jedisPool.getResource();
            list = jedis.hmget(key, fields);
        } catch (Exception e) {
            logger.warn("failed : jedis hmget key : " + key);
            isBroken = true;
        } finally {
            release(jedis, jedisPool, isBroken);
        }
        return list;
    }

    /**
     * 模糊查询
     *
     * @param regex
     * @return
     * @author
     * @2015年3月9日
     */
    public List<String> fuzzyGet(String regex) {
        Jedis jedis = null;
        boolean isBroken = false;
        List<String> list = null;
        try {
            jedis = jedisPool.getResource();
            Set<String> keys = jedis.keys(regex);
            if (keys != null && !keys.isEmpty()) {
                list = new ArrayList<String>();
                for (String key : keys) {
                    list.add(get(key));
                }
            }
        } catch (Exception e) {
            logger.warn("failed : jedis fuzzyGet key : " + regex);
            isBroken = true;
        } finally {
            release(jedis, jedisPool, isBroken);
        }
        return list;
    }

    // set to master
    public void set(String key, String value, int expireTime) {
        Jedis jedis = null;
        boolean isBroken = false;
        try {
            jedis = jedisPool.getResource();
            jedis.set(key, value);
            if (expireTime != 0) {
                jedis.expire(key, expireTime);
            }
        } catch (Exception e) {
            logger.warn("failed : jedis set key : " + key + " , value :" + value + " , expireTime :" + expireTime);
            logger.warn("redis set exception:" + e);
            isBroken = true;
        } finally {
            release(jedis, jedisPool, isBroken);
        }
    }

    // get from slave
    public String get(String key) {
        Jedis jedis = null;
        boolean isBroken = false;
        String value = null;
        try {
            jedis = jedisPool.getResource();
            value = jedis.get(key);
        } catch (Exception e) {
            logger.warn("failed : jedis get key : " + key);
            logger.warn("redis get exception:" + e);
            isBroken = true;
        } finally {
            release(jedis, jedisPool, isBroken);
        }
        return value;
    }

    /**
     * Set the string value as value of the key. The string can't be longer than 1073741824 bytes (1
     * GB).
     *
     * @param key
     * @param value
     * @param nxxx  NX|XX, NX -- Only set the key if it does not already exist. XX -- Only set the key
     *              if it already exist.
     * @param expx  EX|PX, expire time units: EX = seconds; PX = milliseconds
     * @param time  expire time in the units of <code>expx</code>
     * @return Status code reply
     */
    public String set(String key, String value, String nxxx, String expx, long time) {
        Jedis jedis = null;
        String reply = null;
        boolean isBroken = false;
        try {
            jedis = jedisPool.getResource();
//            SetParams.setParams();
//            reply = jedis.set(key, value, nxxx, expx, time);
        } catch (Exception e) {
            logger.warn("failed : jedis get key : " + key);
            isBroken = true;
        } finally {
            release(jedis, jedisPool, isBroken);
        }
        return reply;
    }

    /**
     * Redis can notify Pub/Sub clients about events happening in the key space.
     * This feature is documented at http://redis.io/topics/notifications
     * <p>
     * For instance if keyspace events notification is enabled, and a client
     * performs a DEL operation on key "foo" stored in the Database 0, two
     * messages will be published via Pub/Sub:
     * <p>
     * PUBLISH __keyspace@0__:foo del
     * PUBLISH __keyevent@0__:del foo
     * <p>
     * It is possible to select the events that Redis will notify among a set
     * of classes. Every class is identified by a single character:
     * <p>
     * K     Keyspace events, published with __keyspace@<db>__ prefix.
     * E     Keyevent events, published with __keyevent@<db>__ prefix.
     * g     Generic commands (non-type specific) like DEL, EXPIRE, RENAME, ...
     * $     String commands
     * l     List commands
     * s     Set commands
     * h     Hash commands
     * z     Sorted set commands
     * x     Expired events (events generated every time a key expires)
     * e     Evicted events (events generated when a key is evicted for maxmemory)
     * A     Alias for g$lshzxe, so that the "AKE" string means all the events.
     * <p>
     * The "notify-keyspace-events" takes as argument a string that is composed
     * of zero or multiple characters. The empty string means that notifications
     * are disabled.
     * <p>
     * Example: to enable list and generic events, from the point of view of the
     * event name, use:
     * <p>
     * notify-keyspace-events Elg
     * <p>
     * Example 2: to get the stream of the expired keys subscribing to channel
     * name __keyevent@0__:expired use:
     * <p>
     * notify-keyspace-events Ex
     * <p>
     * By default all notifications are disabled because most users don't need
     * this feature and the feature has some overhead. Note that if you don't
     * specify at least one of K or E, no events will be delivered.
     *
     * @param jedisPubSub
     * @param patterns
     */
    public void psubscribe(final JedisPubSub jedisPubSub, final String... patterns) {
        jedisPool.getResource().psubscribe(jedisPubSub, patterns);
    }

    // del from mater
    public void del(String key) {
        Jedis jedis = null;
        boolean isBroken = false;
        try {
            jedis = jedisPool.getResource();
            jedis.del(key);
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("failed : jedis delete key : " + key);
            isBroken = true;
        } finally {
            release(jedis, jedisPool, isBroken);
        }
    }

    public Long incr(String key) {
        Jedis jedis = null;
        boolean isBroken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.incr(key);
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("failed : jedis incr key : " + key);
            isBroken = true;
        } finally {
            release(jedis, jedisPool, isBroken);
        }
        return -1L;
    }

    //设置在millisecondsTimestamp毫秒时间点失效
    public Long incrForTody(String key, long millisecondsTimestamp) {
        Jedis jedis = null;
        boolean isBroken = false;
        try {
            jedis = jedisPool.getResource();
            long incrRturn = jedis.incr(key);
            if (incrRturn == 1L) {
                jedis.pexpireAt(key, millisecondsTimestamp);
            }
            logger.info("incrRturn ::" + incrRturn);
            return incrRturn;
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("failed : jedis incrForTody key : " + key);
            isBroken = true;
        } finally {
            release(jedis, jedisPool, isBroken);
        }
        return -1L;
    }

    public Long pexpireAt(String key, long millisecondsTimestamp) {
        Jedis jedis = null;
        boolean isBroken = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.pexpireAt(key, millisecondsTimestamp);
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("failed : jedis pexpireAt key : " + key);
            isBroken = true;
        } finally {
            release(jedis, jedisPool, isBroken);
        }
        return -1L;
    }

    //降级查询接口
    public Object getWithDegrade(String key, int expireTime, Callable valueLoader) {
        try {
            Object value = getObj(key);
            if (value != null) {
                return value;
            }
        } catch (Exception e) {
            logger.error("Redis故障", e.getMessage());
            try {
                //redis有故障，执行降级
                return valueLoader.call();
            } catch (Exception ex) {
                e.printStackTrace();
            }
        }
        try {
            Object res = valueLoader.call();
            //异步写入Redis
//            asyncUtils.doAsync(() -> {
//                setObj(key, res, expireTime);
//            });
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    private static final Long RELEASE_SUCCESS = 1L;

    /***
     * 分布式加锁
     * @param key 分布式锁的key
     * @param requestId 分布式锁的value
     *                     很多童鞋可能不明白，有key作为锁不就够了吗，为什么还要用到value？
     *                     原因就是我们在上面讲到可靠性时，分布式锁要满足第四个条件解铃还须系铃人，
     *                     通过给value赋值为requestId，我们就知道这把锁是哪个请求加的了，在解锁的时候就可以有依据
     * @param expireTime 过期时间
     * @return 是否加锁成功
     */

    // 最后是调jedisCleint
    //  nxxx : [NX|XX], 
    //      NX -- Only set the key if it does not already exist. 
    //      XX -- Only set the key if it already exist.
    //  expx    EX|PX, expire time units: EX = seconds; PX = milliseconds
    public boolean tryLock(String key, String requestId, int expireTime) {
        try (Jedis jedis = jedisPool.getResource()) {
//            String result = jedis.set(key, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
//            if (LOCK_SUCCESS.equals(result)) {
//                return true;
//            }
            return false;
        }
    }

    /***
     * 分布式解锁
     * @param key key
     * @param requestId value
     * @return 是否解锁成功
     */
    public boolean unLock(String key, String requestId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Object result = jedis.eval(script, Collections.singletonList(key), Collections.singletonList(requestId));
            if (RELEASE_SUCCESS.equals(result)) {
                return true;
            }
            return false;
        }
    }
}