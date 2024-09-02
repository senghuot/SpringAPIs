package org.example.coffee.repository;

import java.util.List;
import java.util.Random;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.google.gson.Gson;
import kong.unirest.Unirest;
import org.example.coffee.record.JokeResponse;
import org.example.coffee.record.JokeResponses;
import org.example.coffee.service.CassandraDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class gives implementations of create, delete table on Cassandra database
 * Insert & select data from the table
 */
public class JokeRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepository.class);
    private String keyspace = "keyspacedata";
    private String table = "joke";

    /**
     * Create keyspace uprofile in cassandra DB
     */
    public void dropKeyspace() {
        String query = "DROP KEYSPACE IF EXISTS "+keyspace+"";
        CassandraDB.getSession().execute(query);
        LOGGER.info("dropped keyspace '"+keyspace+"'");
    }

    /**
     * Create keyspace uprofile in cassandra DB
     */
    public void createKeyspace() {
        String query = "CREATE KEYSPACE "+keyspace+" WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', 'datacenter1' : 1 }";
        CassandraDB.getSession().execute(query);
        LOGGER.info("Created keyspace '"+keyspace+"'");
    }

    /**
     * Create user table in cassandra DB
     */
    public void createTable() {
        String query = "CREATE TABLE "+keyspace+"."+table+" (id int PRIMARY KEY, joke text)";
        CassandraDB.getSession().execute(query);
        LOGGER.info("Created table '"+table+"'");
    }

    public JokeResponse[] getJokes() {
        var id = new Random().nextInt(690) + 1;
        String ids = "(" + id;
        for (var i=1; i<3; i++) {
            ids += "," + (id + i);
        }

        final String query = "SELECT * FROM "+keyspace+"."+table+" WHERE id IN " + ids + ")";
        var session = CassandraDB.getSession();
        List<Row> rows = session.execute(query).all();
        var jokes = new JokeResponse[3];
        for (var i = 0; i < jokes.length; i++) {
            var joke = new JokeResponse();
            joke.id = "" + rows.get(i).getInt("id");
            joke.joke = rows.get(i).getString("joke");
            jokes[i] = joke;
        }
        return jokes;
    }

    public JokeResponse getJoke(int id) {
        final String query = "SELECT * FROM "+keyspace+"."+table+" where id = " + id;
        Row row = CassandraDB.getSession().execute(query).one();
        var joke = new JokeResponse();
        joke.id = "" + row.getInt("id");
        joke.joke = row.getString("joke");
        return joke;
    }

    public void insertJoke(int id, String joke) {
        var session = CassandraDB.getSession();
        PreparedStatement prepared = session.prepare(prepareInsertStatement());
        BoundStatement bound = prepared.bind(id, joke).setIdempotent(true);
        session.execute(bound);
    }

    /**
     * Create a PrepareStatement to insert a row to user table
     *
     * @return PreparedStatement
     */
    private String prepareInsertStatement() {
        final String insertStatement = "INSERT INTO  "+keyspace+"."+table+" (id, joke) VALUES (?,?)";
        return insertStatement;
    }

    private void seedData() {
        final String keyspace = "keyspacedata";
        final String table = "joke";
        var jokeid = 1;

        try {
            var cassandraSession = CassandraDB.getSession();
            var gson = new Gson();
            for (var i=1; i<=35; i++) {
                var result = Unirest.get("https://icanhazdadjoke.com/search")
                        .header("Accept", "application/json")
                        .queryString("page", i)
                        .queryString("limit", 20)
                        .asString();

                var jokeResponses = gson.fromJson(result.getBody(), JokeResponses.class);
                for (var j = 0; j < jokeResponses.results.length; j++) {
                    insertJoke(jokeid++, jokeResponses.results[j].joke);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Something went wrong: " + e);
        }
        finally {
            LOGGER.info("Please delete your table after verifying the presence of the data in portal or from CQL");
        }
    }
}

