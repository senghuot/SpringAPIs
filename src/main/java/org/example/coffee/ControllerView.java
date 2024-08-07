package org.example.coffee;

import com.google.gson.Gson;
import io.netty.util.internal.StringUtil;
import org.example.coffee.record.JokeResponse;
import org.example.coffee.record.JokeResponses;
import org.example.coffee.service.Redis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import kong.unirest.Unirest;

import java.util.ArrayList;
import java.util.Random;

@Controller
public class ControllerView {

    private static final Logger logger = LoggerFactory.getLogger(Redis.class);
    Gson gson = new Gson();
    Random random = new Random();

    @GetMapping("/")
    public String homepage(Model model) {
        return "index";
    }

    @GetMapping("/joke")
    public String joke(Model model) {
        var jedis = Redis.getJedis();
        long start = System.currentTimeMillis(), end = start;
        var cacheHit = false;
        var response = jedis.lpop("Jokes");
        JokeResponse joke;
        if (StringUtil.isNullOrEmpty(response)) {
            var result = Unirest.get("https://icanhazdadjoke.com/search")
                    .header("Accept", "application/json")
                    .queryString("page", random.nextInt(30))
                    .queryString("limit", 10)
                    .asString();

            var jokeResponses = gson.fromJson(result.getBody(), JokeResponses.class);
            var jokes = new ArrayList<String>();
            for (var i = 0; i < jokeResponses.results.length; i++) {
                var curJoke = jokeResponses.results[i];
                var json = gson.toJson(curJoke);
                jokes.add(json);
            }
            jedis.lpush("Jokes", jokes.toArray(new String[0]));

            end = System.currentTimeMillis();
            joke = jokeResponses.results[0];
        } else {
            end = System.currentTimeMillis();
            logger.info("response: " + response);
            joke = gson.fromJson(response, JokeResponse.class);
            cacheHit = true;
        }
        jedis.close();

        model.addAttribute("joke", joke.joke);
        model.addAttribute("duration", end-start);
        model.addAttribute("id", joke.id);
        model.addAttribute("cacheHit", cacheHit);

        return "joke";
    }
}
