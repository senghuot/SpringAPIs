package org.example.coffee;

import com.google.gson.Gson;
import org.example.coffee.record.JokeResponse;
import org.example.coffee.record.JokeResponses;
import org.example.coffee.service.Redis;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import kong.unirest.Unirest;

import java.util.ArrayList;
import java.util.Random;

@Controller
public class ControllerView {

    Gson gson = new Gson();
    Random random = new Random();

    @GetMapping("/")
    public String homepage(Model model) {
        return "index";
    }

    @GetMapping("/joke")
    public String joke(Model model) {
        long start = System.currentTimeMillis(), end = start;
        var cacheHit = false;
        var response = Redis.pop("Jokes");
        JokeResponse joke;
        if (response == null || response.isEmpty()) {
            var result = Unirest.get("https://icanhazdadjoke.com/search")
                    .header("Accept", "application/json")
                    .queryString("page", random.nextInt(30))
                    .queryString("limit", 5)
                    .asString();

            var jokeResponses = gson.fromJson(result.getBody(), JokeResponses.class);
            var jokes = new ArrayList<String>();
            for (var i = 1; i < jokeResponses.results.length; i++)
                jokes.add(gson.toJson(jokeResponses.results[i]));
            Redis.push("Jokes", jokes.toArray(new String[0]));

            end = System.currentTimeMillis();
            joke = jokeResponses.results[0];
        } else {
            end = System.currentTimeMillis();
            joke = gson.fromJson(response, JokeResponse.class);
            cacheHit = true;
        }

        model.addAttribute("joke", joke.joke);
        model.addAttribute("duration", end-start);
        model.addAttribute("id", joke.id);
        model.addAttribute("cacheHit", cacheHit);

        return "joke";
    }
}
