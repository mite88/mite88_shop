package io.mite88.mite88shop.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        model.addAttribute("productId", id);
        return "products/detail";
    }

    @GetMapping("/cart")
    public String cart() {
        return "cart/index";
    }

    @GetMapping("/orders")
    public String orders() {
        return "orders/index";
    }
}
