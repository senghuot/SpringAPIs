package org.example.coffee.service;

import com.datastax.oss.driver.api.core.CqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.net.InetSocketAddress;
import java.security.*;

public class CassandraDB {

    private static String cassandraHost = KV.getSecret("cassandraHost");
    private static String cassandraUsername = KV.getSecret("cassandraUsername");
    private static String cassandraPassword = KV.getSecret("cassandraPassword");
    private static String region = "East US";
    private static int cassandraPort = 10350;
    private static CqlSession session;

    private static final Logger logger = LoggerFactory.getLogger(CqlSession.class);

    public static CqlSession getSession() {
        try {
            if (session == null)
                warmup();
        } catch (Exception e) {
            logger.error("[CassandraDB.java] Exception: ", e);
        }
        return session;
    }

    public static void warmup() throws NoSuchAlgorithmException {
        SSLContext sslContext = SSLContext.getDefault();
        session = CqlSession.builder()
                .withSslContext(sslContext)
                .addContactPoint(new InetSocketAddress(cassandraHost, cassandraPort)).withLocalDatacenter(region)
                .withAuthCredentials(cassandraUsername, cassandraPassword).build();
    }
}
