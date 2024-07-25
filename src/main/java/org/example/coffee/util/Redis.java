package org.example.coffee.util;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.params.SetParams;

public class Redis {

    static SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl("https://key-queue.vault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

    private static JedisPool redisPool = new JedisPool(new HostAndPort("scli.redis.cache.windows.net", 6380),
            DefaultJedisClientConfig.builder()
                    .ssl(true)
                    .password(KeyVault.getSecret("redisconnectionstring"))
                    .build());
    private static Jedis jedis = redisPool.getResource();

    public static String set(final String key, final String val) {
        return jedis.set(key, val, SetParams.setParams().ex(5));
    }

    public static String get(final String key) {
        try {
            return jedis.get(key);
        } catch (JedisConnectionException e) {
            jedis.close();
            jedis = redisPool.getResource();
        }
        return jedis.get(key);
    }
}
