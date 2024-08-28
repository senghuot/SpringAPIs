package org.example.coffee;

import org.example.coffee.record.Message;
import org.example.coffee.service.Queue;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.google.gson.Gson;

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
}
