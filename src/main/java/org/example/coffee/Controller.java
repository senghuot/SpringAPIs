package org.example.coffee;

import com.google.gson.Gson;
import kong.unirest.Unirest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
        var response = Unirest.get("https://icanhazdadjoke.com/")
                .header("Accept", "application/json")
                .asString();
        var jokeResponse = gson.fromJson(response.getBody(), JokeResponse.class);
        return jokeResponse.joke;
    }
}

class JokeResponse {
    String joke, id;
    int status;
}
