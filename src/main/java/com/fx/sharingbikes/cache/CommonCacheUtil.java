package com.fx.sharingbikes.cache;

import com.fx.sharingbikes.common.exception.SharingBikesException;
import com.fx.sharingbikes.user.entity.UserElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.Map;

@Component
@Slf4j
public class CommonCacheUtil {

    @Autowired
    private JedisPoolWrapper jedisPoolWrapper;

    private static final String TOKEN_PREFIX = "token.";

    private static final String USER_PREFIX = "user.";

    public void cache(String key, String value) {
        try {
            JedisPool pool = jedisPoolWrapper.getJedisPool();
            if (pool != null) {
                try (Jedis Jedis = pool.getResource()) {
                    Jedis.select(0);
                    Jedis.set(key, value);
                }
            }
        } catch (Exception e) {
            log.error("Fail to cache value", e);
        }
    }

    public String getCacheValue(String key) {
        String value = null;
        try {
            JedisPool pool = jedisPoolWrapper.getJedisPool();
            if (pool != null) {
                try (Jedis Jedis = pool.getResource()) {
                    Jedis.select(0);
                    value = Jedis.get(key);
                }
            }
        } catch (Exception e) {
            log.error("Fail to get cached value", e);
        }
        return value;
    }

    public long cacheNxExpire(String key, String value, int expiry) {
        long result = 0;
        try {
            JedisPool pool = jedisPoolWrapper.getJedisPool();
            if (pool != null) {
                try (Jedis jedis = pool.getResource()) {
                    jedis.select(0);
                    result = jedis.setnx(key, value);
                    jedis.expire(key, expiry);
                }
            }
        } catch (Exception e) {
            log.error("Fail to cacheNx value", e);
        }
        return result;
    }

    public void delKey(String key) {
        JedisPool pool = jedisPoolWrapper.getJedisPool();
        if (pool != null) {
            try (Jedis jedis = pool.getResource()) {
                jedis.select(0);
                try {
                    jedis.del(key);
                } catch (Exception e) {
                    log.error("Fail to remove key from redis", e);
                }
            } catch (Exception e) {
                log.error("Fail to remove key from redis", e);
            }
        }
    }

    public void putTokenWhenLogin(UserElement userElement) {
        JedisPool pool = jedisPoolWrapper.getJedisPool();
        if (pool != null) {
            try (Jedis jedis = pool.getResource()) {
                jedis.select(0);
                Transaction trans = jedis.multi();
                try {
                    trans.del(TOKEN_PREFIX + userElement.getToken());
                    trans.hmset(TOKEN_PREFIX + userElement.getToken(), userElement.toMap());
                    trans.expire(TOKEN_PREFIX + userElement.getToken(), 2592000);
                    trans.sadd(USER_PREFIX + userElement.getUserId(), userElement.getToken());
                    trans.exec();
                } catch (Exception e) {
                    trans.discard();
                    log.error("Fail to cache token to redis", e);
                }
            }
        }
    }

    public UserElement getUserByToken(String token) {
        UserElement userElement = null;
        JedisPool pool = jedisPoolWrapper.getJedisPool();
        if (pool != null) {
            try (Jedis jedis = pool.getResource()) {
                jedis.select(0);
                try {
                    Map<String, String> map = jedis.hgetAll(TOKEN_PREFIX + token);
                    if (!CollectionUtils.isEmpty(map)) {
                        userElement = UserElement.fromMap(map);
                    } else {
                        log.warn("Fail to find cached element for token");
                    }
                } catch (Exception e) {
                    log.error("Fail to get user by token in redis", e);
                    throw e;
                }
            }
        }
        return userElement;
    }

    public int cacheForVerificationCode(String key, String verCode, String type, int second, String ip) throws SharingBikesException {
        JedisPool pool = jedisPoolWrapper.getJedisPool();
        if (pool != null) {
            try (Jedis jedis = pool.getResource()) {
                jedis.select(0);
                try {
                    String ipKey = "ip." + ip;
                    if (ip == null) {
                        return 3;
                    } else {
                        String ipSendCount = jedis.get(ipKey);
                        try {
                            if (ipSendCount != null && Integer.parseInt(ipSendCount) >= 10) {
                                return 3;
                            }
                        } catch (NumberFormatException e) {
                            log.error("Fail to process ip send count", e);
                            return 3;
                        }
                        long succ = jedis.setnx(key, verCode);
                        if (succ == 0) {
                            return 1;
                        }
                        String sendCount = jedis.get(key + "." + type);
                        try {
                            if (sendCount != null && Integer.parseInt(sendCount) >= 10) {
                                jedis.del(key);
                                return 2;
                            }
                        } catch (NumberFormatException e) {
                            log.error("Fail to process send count", e);
                            jedis.del(key);
                            return 2;
                        }
                        try {
                            jedis.expire(key, second);
                            long val = jedis.incr(key + "." + type);
                            if (val == 1) {
                                jedis.expire(key + "." + type, 86400);
                            }
                            jedis.incr(ipKey);
                            if (val == 1) {
                                jedis.expire(ipKey, 86400);
                            }
                        } catch (Exception e) {
                            log.error("Fail to cache data into redis", e);
                        }
                    }
                } catch (Exception e) {
                    log.error("Fail to set vercode to redis", e);
                    throw e;
                }
            } catch (Exception e) {
                log.error("Fail to cache for expiry", e);
                throw new SharingBikesException("Fail to cache for expiry");
            }
        }
        return 0;
    }
}
