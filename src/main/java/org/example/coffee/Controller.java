package org.example.coffee;

import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import com.google.gson.Gson;
import kong.unirest.Unirest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.params.SetParams;

import java.util.Random;

@RestController
public class Controller {
    Gson gson = new Gson();
    Random ran = new Random();

    @GetMapping(value = "/hello")
    public String index() {
        return "Greetings from Spring Boot!";
    }

    @GetMapping(value = "/random")
    public String random() {
        var n = ran.nextInt();
        return "random num: " + n;
    }

    @GetMapping(value = "/joke")
    public String joke() {
        var start = System.currentTimeMillis();
        var response = Unirest.get("https://icanhazdadjoke.com/")
                .header("Accept", "application/json")
                .asString();
        var end = System.currentTimeMillis();
        var jokeResponse = gson.fromJson(response.getBody(), JokeResponse.class);
        return String.format("Joke: \"%s\" <br> Duration: %s ms ", jokeResponse.joke, end-start);
    }

    @GetMapping(value = "/joke-redis")
    public String jokeRedis() {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        JokeResponse jokeResponse = null;
        long duration = 0;
        var start = System.currentTimeMillis();
        var joke = jedis.get("joke");
        if (joke != null && !joke.isEmpty()) {
            duration = System.currentTimeMillis() - start;
            jokeResponse = gson.fromJson(joke, JokeResponse.class);
        } else {
            start = System.currentTimeMillis();
            var response = Unirest.get("https://icanhazdadjoke.com/")
                    .header("Accept", "application/json")
                    .asString();
            duration = System.currentTimeMillis() - start;
            jedis.set("joke", response.getBody());
            jedis.expire("joke", 5);
            jokeResponse = gson.fromJson(response.getBody(), JokeResponse.class);
        }
        return String.format("Joke: \"%s\" <br> Duration: %s ms ", jokeResponse.joke, duration);
    }
}

class JokeResponse {
    String joke, id;
    int status;
}
