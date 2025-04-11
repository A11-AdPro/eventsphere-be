package id.ac.ui.cs.advprog.eventsphere.review.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReviewDtoTest {

    @Test
    void testGettersAndSetters() {
        // Given
        ReviewDto reviewDto = new ReviewDto();
        int rating = 2;
        String comment = "Could be better";

        // When
        reviewDto.setRating(rating);
        reviewDto.setComment(comment);

        // Then
        assertEquals(rating, reviewDto.getRating());
        assertEquals(comment, reviewDto.getComment());
    }
}
