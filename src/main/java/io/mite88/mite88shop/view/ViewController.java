package io.mite88.mite88shop.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

    /**
     * 메인 페이지
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * 회원가입 페이지
     */
    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    /**
     * 상품 상세 페이지 - productId를 모델에 담아 Thymeleaf에서 API 호출에 사용
     */
    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        model.addAttribute("productId", id);
        return "products/detail";
    }

    /**
     * Q&A 게시판 목록 페이지
     */
    @GetMapping("/qa")
    public String qa() {
        return "posts/index";
    }

    /**
     * Q&A 게시글 상세 페이지
     */
    @GetMapping("/qa/{id}")
    public String qaDetail(@PathVariable Long id, Model model) {
        model.addAttribute("postId", id);
        return "posts/detail";
    }

    /**
     * 장바구니 페이지
     */
    @GetMapping("/cart")
    public String cart() {
        return "cart/index";
    }

    /**
     * 주문 목록 페이지
     */
    @GetMapping("/orders")
    public String orders() {
        return "orders/index";
    }
}
