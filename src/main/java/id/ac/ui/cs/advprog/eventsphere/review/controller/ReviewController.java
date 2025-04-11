package id.ac.ui.cs.advprog.eventsphere.review.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import id.ac.ui.cs.advprog.eventsphere.review.service.ReviewService;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewDto;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/reviews")
    public ResponseEntity<Map<String, Object>> createReview(@RequestBody ReviewDto reviewDto) {
        reviewService.createReview(reviewDto.getRating(), reviewDto.getComment());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Review submitted successfully");
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
