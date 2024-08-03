package org.example.coffee;

import org.example.coffee.record.JokeResponse;
import org.example.coffee.record.Message;
import org.example.coffee.service.Queue;
import org.example.coffee.service.Redis;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.google.gson.Gson;
import kong.unirest.Unirest;

import java.util.Map;

@RestController
@Validated
public class ControllerRest {

    Gson gson = new Gson();

    @PostMapping(value = "/push")
    public ResponseEntity<String> Push(@RequestBody Message json) {
        if (!Queue.push(json.message))
            return ResponseEntity.badRequest().body("Message cannot be null or empty");
        return ResponseEntity.ok("Received: " + json.message);
    }

    @GetMapping(value = "/pop")
    public String Pop() {
        return Queue.pop();
    }

    @GetMapping(value = "/redis/push")
    public ResponseEntity<String> redisPush(@RequestParam Map<String, String> params) {
        var msg = params.get("message");
        if (msg == null || msg.isEmpty())
            return ResponseEntity.badRequest().body("Message cannot be null or empty");
        var ith = Redis.push(msg);
        return ResponseEntity.ok("Received: " + msg + "<br>" + "Position: " + ith);
    }

    @GetMapping(value = "/redis/pop")
    public ResponseEntity<String> redisPop() {
        var msg = Redis.pop();
        return ResponseEntity.ok( msg == null ? "NO_NEW_MESSAGE" : msg);
    }
}
