package org.example.coffee.service;

import com.datastax.oss.driver.api.core.CqlSession;
import javax.net.ssl.*;
import java.net.InetSocketAddress;
import java.security.*;
import java.util.LinkedList;

public class CassandraDB {

    private static String cassandraHost = KV.getSecret("cassandraHost");
    private static String cassandraUsername = KV.getSecret("cassandraUsername");
    private static String cassandraPassword = KV.getSecret("cassandraPassword");
    private static String region = "East US";
    private static int cassandraPort = 10350;

    private static LinkedList<CqlSession> pool = new LinkedList();

    public static void warmup() throws NoSuchAlgorithmException {
        for (var i=0; i<1; i++) {
            pool.add(createSession());
        }
    }

    public static CqlSession getSession() throws NoSuchAlgorithmException {
        if (!pool.isEmpty()) {
            return pool.remove();
        }
        return createSession();
    }

    public static void closeSession(CqlSession session) {
        pool.add(session);
    }

    private static CqlSession createSession() throws NoSuchAlgorithmException {
        SSLContext sslContext = SSLContext.getDefault();
        return CqlSession.builder()
                .withSslContext(sslContext)
                .addContactPoint(new InetSocketAddress(cassandraHost, cassandraPort)).withLocalDatacenter(region)
                .withAuthCredentials(cassandraUsername, cassandraPassword).build();
    }
}
