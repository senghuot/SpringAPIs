package org.example.coffee.service;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.params.SetParams;

public class Redis {

    private static final Logger logger = LoggerFactory.getLogger(Redis.class);

    static SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl("https://key-queue.vault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

    static JedisPoolConfig poolConfig = new JedisPoolConfig();
    static {
        poolConfig.setMinIdle(20); // up to 20 idle connections ready to serve
        poolConfig.setMaxTotal(50); // total connection of idel + active, control perf and cogs
        // poolConfig.setMaxIdle(25);
    }

    private static JedisPool redisPool = new JedisPool(poolConfig,
            new HostAndPort("scli.redis.cache.windows.net", 6380),
            DefaultJedisClientConfig.builder()
                    .ssl(true)
                    .password(KV.getSecret("redisconnectionstring"))
                    .build());

    public static String warmup() {
        try {
            var jedis = redisPool.getResource();
            var response = jedis.ping();
            jedis.close();
            return response;
        } catch (JedisConnectionException e) {
            logger.warn("[Redis.java] JedisConnectionException: ", e);
        }
        return "";
    }
    public static long push(final String key, final String[] val) {
        for (var i=0; i<5; i++) {
            try {
                var jedis = redisPool.getResource();
                var response = jedis.lpush(key, val);
                jedis.close();
                return response;
            } catch (JedisConnectionException e) {
                logger.warn("[Redis.java] JedisConnectionException: ", e);
            }
        }
        return -1;
    }

    public static String pop(String name) {
        for (var i=0; i<5; i++) {
            try {
                var jedis = redisPool.getResource();
                var response = jedis.lpop(name);
                jedis.close();
                return response;
            } catch (JedisConnectionException e) {
                logger.warn("[Redis.java] JedisConnectionException: ", e);
            }
        }
        return "";
    }
}
