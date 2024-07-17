package org.example.coffee;

import com.google.gson.Gson;
import kong.unirest.Unirest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    Gson gson = new Gson();

    @GetMapping(value = "/hello")
    public String index() {
        System.out.println("we just got here");
        return "Greetings from Spring Boot!";
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
