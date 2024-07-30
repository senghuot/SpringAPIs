package org.example.coffee;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControllerView {

    @GetMapping("/")
    public String homepage(Model model) {
        model.addAttribute("message", "Welcome to this motherfriking website");
        return "index";
    }
}
