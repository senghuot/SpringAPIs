package org.example.coffee;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.Jedis;
import com.google.gson.Gson;
import kong.unirest.Unirest;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

import java.time.Duration;
import java.util.Random;

@RestController
@Validated
public class Controller {
//    SecretClient secretClient = new SecretClientBuilder()
//            .vaultUrl("https://key-queue.vault.azure.net/")
//            .credential(new DefaultAzureCredentialBuilder().build())
//            .buildClient();

    Jedis jedis = new Jedis("scli.redis.cache.windows.net", 6380, DefaultJedisClientConfig
            .builder()
            .ssl(true)
            .password(System.getenv("redisconnectionstring"))
            .build());

    private final String connectionString = System.getenv("qconnectionstring");
    private final String queueName = "op";

    ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName(queueName)
            .buildClient();

    ServiceBusReceiverClient receiverClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .queueName(queueName)
            .buildClient();

    Gson gson = new Gson();
    Random ran = new Random();

    @GetMapping(value = "/")
    public String index() {
        return "<h1>Welcome to this motherfriking website</h1>" +
                "<ul><li>Shit's lightweight and loads fast</li>" +
                "<li>Fits on all your shitty screens</li>" +
                "<li>Looks the same in all your shitty browsers</li>" +
                "<li>The motherfucker's accessible to every asshole that visits your site</li>" +
                "<li>Shit's legible and gets your fucking point across</li>" +
                "<li>Relax and read this <a href=\"/joke\">dad's joke</a></li>";
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

    @PostMapping(value = "/v1/push")
    public ResponseEntity<String> PushMessage(@RequestBody Message json) {
        if (json.message == null || json.message.isEmpty())
            return ResponseEntity.badRequest().body("Message cannot be null or empty");
        senderClient.sendMessage(new ServiceBusMessage(json.message));
        return ResponseEntity.ok("Received: " + json.message);
    }

    @GetMapping(value = "/v1/pop")
    public String PushMessage() {
        var messages = receiverClient.receiveMessages(1, Duration.ofSeconds(3));
        for (var message: messages) {
            return message.getBody().toString();
        }
        return "NO_NEW_MESSAGE";
    }


    @GetMapping(value = "/joke-redis")
    public String jokeRedis() {
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

class Message {
    public String message;
}
