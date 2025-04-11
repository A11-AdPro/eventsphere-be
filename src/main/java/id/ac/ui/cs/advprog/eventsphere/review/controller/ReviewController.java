package id.ac.ui.cs.advprog.eventsphere.review.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import id.ac.ui.cs.advprog.eventsphere.review.service.ReviewService;

@RestController
@RequestMapping("/api")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/reviews")
    public void createReview(@RequestParam int rating, @RequestParam String comment) {
        reviewService.createReview(rating, comment);
    }
}
