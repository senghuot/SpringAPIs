package org.example.coffee.service;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.params.SetParams;

public class Redis {

    private static final Logger logger = LoggerFactory.getLogger(Redis.class);

    static SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl("https://key-queue.vault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

    private static JedisPool redisPool = new JedisPool(new HostAndPort("scli.redis.cache.windows.net", 6380),
            DefaultJedisClientConfig.builder()
                    .ssl(true)
                    .password(KV.getSecret("redisconnectionstring"))
                    .build());

    public static long push(final String key, final String[] val) {
        Jedis jedis = null;
        long response = 0;
        try {
            jedis = redisPool.getResource();
            response = jedis.lpush(key, val);
        } catch (JedisConnectionException e) {
            jedis = redisPool.getResource();
            response = jedis.lpush(key, val);
            logger.warn("JedisConnectionException: ", e);
        }
        jedis.close();
        return response;
    }

    public static String pop(String name) {
        Jedis jedis = null;
        String response = "";
        try {
            jedis = redisPool.getResource();
            response = jedis.lpop(name);
        } catch (JedisConnectionException e) {
            jedis = redisPool.getResource();
            response = jedis.lpop(name);
            logger.warn("JedisConnectionException: ", e);
        }
        jedis.close();
        return response;
    }
}
