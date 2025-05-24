package id.ac.ui.cs.advprog.eventsphere.review.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.service.AuthService;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewReportRequest;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewRequest;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewResponseRequest;
import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewCreatedEvent;
import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewDeletedEvent;
import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewReportedEvent;
import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewUpdatedEvent;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewImage;
import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewReport;
import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewResponse;
import id.ac.ui.cs.advprog.eventsphere.review.repository.ReviewImageRepository;
import id.ac.ui.cs.advprog.eventsphere.review.repository.ReviewReportRepository;
import id.ac.ui.cs.advprog.eventsphere.review.repository.ReviewRepository;
import id.ac.ui.cs.advprog.eventsphere.review.repository.ReviewResponseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ReviewImageRepository imageRepository;
    @Mock
    private ReviewResponseRepository responseRepository;
    @Mock
    private ReviewReportRepository reportRepository;
    @Mock
    private AuthService authService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User attendeeUser;
    private User adminUser;
    private User organizerUser;
    private Review review;
    private ReviewRequest reviewRequest;
    private Path mockFileStoragePath = Paths.get("test-uploads/reviews").toAbsolutePath().normalize();

    @BeforeEach
    void setUp() {
        attendeeUser = User.builder().id(1L).email("attendee@example.com").role(Role.ATTENDEE).build();
        adminUser = User.builder().id(2L).email("admin@example.com").role(Role.ADMIN).build();
        organizerUser = User.builder().id(3L).email("organizer@example.com").role(Role.ORGANIZER).build();

        review = Review.builder()
                .id(1L)
                .content("Great event!")
                .rating(5)
                .eventId(101L)
                .ticketId(202L)
                .user(attendeeUser)
                .createdAt(LocalDateTime.now().minusDays(1)) // Editable
                .isVisible(true)
                .isReported(false)
                .images(new ArrayList<>())
                .build();

        reviewRequest = new ReviewRequest();
        reviewRequest.setEventId(101L);
        reviewRequest.setTicketId(202L);
        reviewRequest.setContent("Great event!");
        reviewRequest.setRating(5);
        reviewRequest.setImages(Collections.emptyList());
    }

    @Test
    void createReview_Success() {
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        when(reviewRepository.existsByUserIdAndEventId(attendeeUser.getId(), reviewRequest.getEventId())).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Review createdReview = reviewService.createReview(reviewRequest);

        assertNotNull(createdReview);
        assertEquals(reviewRequest.getContent(), createdReview.getContent());
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(eventPublisher, times(1)).publishEvent(any(ReviewCreatedEvent.class));
    }

    @Test
    void createReview_SuccessWithImages() throws IOException {
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        when(reviewRepository.existsByUserIdAndEventId(attendeeUser.getId(), reviewRequest.getEventId())).thenReturn(false);

        Review initialSavedReview = Review.builder()
                .id(1L)
                .content(reviewRequest.getContent())
                .rating(reviewRequest.getRating())
                .eventId(reviewRequest.getEventId())
                .ticketId(reviewRequest.getTicketId())
                .user(attendeeUser)
                .isReported(false)
                .isVisible(true)
                .images(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(reviewRepository.save(any(Review.class))).thenReturn(initialSavedReview);
        when(reviewRepository.findById(initialSavedReview.getId())).thenReturn(Optional.of(initialSavedReview));

        MockMultipartFile imageFile = new MockMultipartFile("images", "image1.jpg", "image/jpeg", "content".getBytes());
        reviewRequest.setImages(List.of(imageFile));

        String fixedUuid = "fixed-uuid-for-create";
        String expectedOriginalFilename = imageFile.getOriginalFilename();
        String expectedStoredFilename = fixedUuid + "_" + expectedOriginalFilename;

        Path serviceFileStoragePath = Paths.get("uploads/reviews").toAbsolutePath().normalize();
        Path expectedTargetLocation = serviceFileStoragePath.resolve(expectedStoredFilename);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class);
             MockedStatic<UUID> mockedUuid = mockStatic(UUID.class)) {

            UUID mockUUIDObj = mock(UUID.class);
            when(mockUUIDObj.toString()).thenReturn(fixedUuid);
            mockedUuid.when(UUID::randomUUID).thenReturn(mockUUIDObj);

            mockedFiles.when(() -> Files.createDirectories(eq(serviceFileStoragePath))).thenReturn(serviceFileStoragePath);
            mockedFiles.when(() -> Files.copy(any(InputStream.class), eq(expectedTargetLocation))).thenReturn(1L);

            ReviewImage savedImageEntity = ReviewImage.builder()
                    .id(100L)
                    .fileName(expectedStoredFilename)
                    .filePath(expectedTargetLocation.toString())
                    .contentType(imageFile.getContentType())
                    .review(initialSavedReview)
                    .build();

            when(imageRepository.save(argThat(ri ->
                    ri.getFileName().equals(expectedStoredFilename) &&
                    ri.getFilePath().equals(expectedTargetLocation.toString()) &&
                    ri.getReview().getId().equals(initialSavedReview.getId()) &&
                    ri.getContentType().equals(imageFile.getContentType())
            ))).thenReturn(savedImageEntity);

            when(imageRepository.countByReviewId(initialSavedReview.getId())).thenReturn(0);

            Review result = reviewService.createReview(reviewRequest);

            assertNotNull(result);
            assertEquals(initialSavedReview.getId(), result.getId());
            verify(reviewRepository, times(1)).save(any(Review.class));
            verify(imageRepository, times(1)).save(any(ReviewImage.class));
            verify(eventPublisher, times(1)).publishEvent(any(ReviewCreatedEvent.class));

            mockedFiles.verify(() -> Files.createDirectories(eq(serviceFileStoragePath)));
            mockedFiles.verify(() -> Files.copy(any(InputStream.class), eq(expectedTargetLocation)));
            mockedUuid.verify(UUID::randomUUID);
        }
    }

    @Test
    void createReview_UserNotAttendee_ThrowsAccessDeniedException() {
        when(authService.getCurrentUser()).thenReturn(adminUser);

        assertThrows(AccessDeniedException.class, () -> {
            reviewService.createReview(reviewRequest);
        });
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_UserHasNotAttendedEvent_ThrowsIllegalArgumentException() {
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        reviewRequest.setTicketId(null);

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createReview(reviewRequest);
        });
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_UserAlreadyReviewed_ThrowsIllegalArgumentException() {
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        when(reviewRepository.existsByUserIdAndEventId(attendeeUser.getId(), reviewRequest.getEventId())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createReview(reviewRequest);
        });
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void getReviewsByEventIdSorted_SortByHighest() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> expectedPage = new PageImpl<>(Collections.singletonList(review));
        when(reviewRepository.findByEventIdOrderByRatingDesc(101L, pageable)).thenReturn(expectedPage);

        Page<Review> result = reviewService.getReviewsByEventIdSorted(101L, "highest", pageable);

        assertEquals(expectedPage, result);
        verify(reviewRepository).findByEventIdOrderByRatingDesc(101L, pageable);
    }

    @Test
    void getReviewsByEventIdSorted_SortByLowest() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> expectedPage = new PageImpl<>(Collections.singletonList(review));
        when(reviewRepository.findByEventIdOrderByRatingAsc(101L, pageable)).thenReturn(expectedPage);

        Page<Review> result = reviewService.getReviewsByEventIdSorted(101L, "lowest", pageable);

        assertEquals(expectedPage, result);
        verify(reviewRepository).findByEventIdOrderByRatingAsc(101L, pageable);
    }

    @Test
    void getReviewsByEventIdSorted_SortByDefault() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> expectedPage = new PageImpl<>(Collections.singletonList(review));
        when(reviewRepository.findByEventIdOrderByCreatedAtDesc(101L, pageable)).thenReturn(expectedPage);

        Page<Review> result = reviewService.getReviewsByEventIdSorted(101L, "some_other_sort", pageable);

        assertEquals(expectedPage, result);
        verify(reviewRepository).findByEventIdOrderByCreatedAtDesc(101L, pageable);
    }

    @Test
    void getReviewsByEventIdSorted_SortByNewest() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> expectedPage = new PageImpl<>(Collections.singletonList(review));
        when(reviewRepository.findByEventIdOrderByCreatedAtDesc(101L, pageable)).thenReturn(expectedPage);

        Page<Review> result = reviewService.getReviewsByEventIdSorted(101L, "newest", pageable);

        assertEquals(expectedPage, result);
        verify(reviewRepository).findByEventIdOrderByCreatedAtDesc(101L, pageable);
    }

    @Test
    void updateReview_ReviewNotFound_ThrowsIllegalArgumentException() {
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.updateReview(1L, reviewRequest);
        });
    }

    @Test
    void updateReview_UserNotOwnerNorAdmin_ThrowsAccessDeniedException() {
        User anotherUser = User.builder().id(99L).role(Role.ATTENDEE).build();
        review.setUser(anotherUser);
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThrows(AccessDeniedException.class, () -> {
            reviewService.updateReview(1L, reviewRequest);
        });
    }

    @Test
    void updateReview_NotAdminAndCannotEdit_ThrowsIllegalArgumentException() {
        review.setCreatedAt(LocalDateTime.now().minusDays(8));
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.updateReview(1L, reviewRequest);
        });
    }

    @Test
    void updateReview_Success_ByUserOwner() {
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        reviewRequest.setContent("Updated Content");
        reviewRequest.setRating(4);

        Review updatedReview = reviewService.updateReview(1L, reviewRequest);

        assertNotNull(updatedReview);
        assertEquals("Updated Content", updatedReview.getContent());
        assertEquals(4, updatedReview.getRating());
        verify(eventPublisher).publishEvent(any(ReviewUpdatedEvent.class));
    }

    @Test
    void updateReview_Success_ByAdmin() {
        review.setCreatedAt(LocalDateTime.now().minusDays(8));
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        reviewRequest.setContent("Admin Updated Content");

        Review updatedReview = reviewService.updateReview(1L, reviewRequest);

        assertNotNull(updatedReview);
        assertEquals("Admin Updated Content", updatedReview.getContent());
        verify(eventPublisher).publishEvent(any(ReviewUpdatedEvent.class));
    }

    @Test
    void updateReview_SuccessWithImages() throws IOException {
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile imageFile = new MockMultipartFile("images", "new_image.jpg", "image/jpeg", "new_content".getBytes());
        reviewRequest.setImages(List.of(imageFile));
        reviewRequest.setContent("Updated with image");

        String fixedUuid = "fixed-uuid-for-update";
        String expectedOriginalFilename = imageFile.getOriginalFilename();
        String expectedStoredFilename = fixedUuid + "_" + expectedOriginalFilename;

        Path serviceFileStoragePath = Paths.get("uploads/reviews").toAbsolutePath().normalize();
        Path expectedTargetLocation = serviceFileStoragePath.resolve(expectedStoredFilename);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class);
             MockedStatic<UUID> mockedUuid = mockStatic(UUID.class)) {

            UUID mockUUIDObj = mock(UUID.class);
            when(mockUUIDObj.toString()).thenReturn(fixedUuid);
            mockedUuid.when(UUID::randomUUID).thenReturn(mockUUIDObj);

            mockedFiles.when(() -> Files.createDirectories(eq(serviceFileStoragePath))).thenReturn(serviceFileStoragePath);
            mockedFiles.when(() -> Files.copy(any(InputStream.class), eq(expectedTargetLocation))).thenReturn(1L);

            ReviewImage savedImage = ReviewImage.builder()
                    .fileName(expectedStoredFilename)
                    .filePath(expectedTargetLocation.toString())
                    .contentType(imageFile.getContentType())
                    .review(review)
                    .build();
            when(imageRepository.save(argThat(ri ->
                ri.getFileName().equals(expectedStoredFilename) &&
                ri.getFilePath().equals(expectedTargetLocation.toString()) &&
                ri.getReview().getId().equals(review.getId())
            ))).thenReturn(savedImage);
            when(imageRepository.countByReviewId(review.getId())).thenReturn(0);

            Review result = reviewService.updateReview(1L, reviewRequest);

            assertNotNull(result);
            assertEquals("Updated with image", result.getContent());
            verify(reviewRepository, times(1)).save(any(Review.class));
            verify(imageRepository, times(1)).save(any(ReviewImage.class));
            verify(eventPublisher, times(1)).publishEvent(any(ReviewUpdatedEvent.class));

            mockedFiles.verify(() -> Files.createDirectories(eq(serviceFileStoragePath)));
            mockedFiles.verify(() -> Files.copy(any(InputStream.class), eq(expectedTargetLocation)));
            mockedUuid.verify(UUID::randomUUID);
        }
    }

    @Test
    void deleteReview_ReviewNotFound_ThrowsIllegalArgumentException() {
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.deleteReview(1L);
        });
    }

    @Test
    void deleteReview_UserNotOwnerNorAdmin_ThrowsAccessDeniedException() {
        User anotherUser = User.builder().id(99L).role(Role.ATTENDEE).build();
        review.setUser(anotherUser);
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThrows(AccessDeniedException.class, () -> {
            reviewService.deleteReview(1L);
        });
    }

    @Test
    void deleteReview_Success_ByUserOwner() {
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        doNothing().when(reviewRepository).delete(review);

        reviewService.deleteReview(1L);

        verify(reviewRepository).delete(review);
        verify(eventPublisher).publishEvent(any(ReviewDeletedEvent.class));
    }

    @Test
    void deleteReview_Success_ByAdmin() {
        User anotherUser = User.builder().id(99L).role(Role.ATTENDEE).build();
        review.setUser(anotherUser);
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        doNothing().when(reviewRepository).delete(review);

        reviewService.deleteReview(1L);

        verify(reviewRepository).delete(review);
        verify(eventPublisher).publishEvent(any(ReviewDeletedEvent.class));
    }

    @Test
    void hasUserAttendedEvent_WithTicketId_ReturnsTrue() {
        assertTrue(reviewService.hasUserAttendedEvent(1L, 101L, 202L));
    }

    @Test
    void hasUserAttendedEvent_NullTicketId_ReturnsFalse() {
        assertFalse(reviewService.hasUserAttendedEvent(1L, 101L, null));
    }

    @Test
    void canEditReview_ReviewNotFound_ThrowsIllegalArgumentException() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.canEditReview(1L);
        });
    }

    @Test
    void canEditReview_CanEdit_ReturnsTrue() {
        review.setCreatedAt(LocalDateTime.now().minusDays(1));
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        assertTrue(reviewService.canEditReview(1L));
    }

    @Test
    void canEditReview_CannotEdit_ReturnsFalse() {
        review.setCreatedAt(LocalDateTime.now().minusDays(8));
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        assertFalse(reviewService.canEditReview(1L));
    }

    @Test
    void addImagesToReview_ReviewNotFound_ThrowsIllegalArgumentException() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());
        List<MultipartFile> images = List.of(mock(MultipartFile.class));
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.addImagesToReview(1L, images);
        });
    }

    @Test
    void addImagesToReview_TooManyImages_ThrowsIllegalArgumentException() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(imageRepository.countByReviewId(1L)).thenReturn(2);
        List<MultipartFile> images = List.of(mock(MultipartFile.class), mock(MultipartFile.class));

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.addImagesToReview(1L, images);
        });
    }

    @Test
    void addImagesToReview_CreateDirectoryFails_ThrowsRuntimeException() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(imageRepository.countByReviewId(1L)).thenReturn(0);
        MockMultipartFile imageFile = new MockMultipartFile("images", "image1.jpg", "image/jpeg", "content".getBytes());
        List<MultipartFile> images = List.of(imageFile);

        Path expectedPathInService = Paths.get("uploads/reviews").toAbsolutePath().normalize();

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.createDirectories(expectedPathInService))
                       .thenThrow(IOException.class);

            assertThrows(RuntimeException.class, () -> {
                reviewService.addImagesToReview(1L, images);
            });

            mockedFiles.verify(() -> Files.createDirectories(expectedPathInService));
        }
    }

    @Test
    void addImagesToReview_InvalidImageType_ThrowsIllegalArgumentException() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(imageRepository.countByReviewId(1L)).thenReturn(0);
        MockMultipartFile invalidFile = new MockMultipartFile("images", "image.txt", "text/plain", "content".getBytes());
        List<MultipartFile> images = List.of(invalidFile);

        Path serviceFileStoragePath = Paths.get("uploads/reviews").toAbsolutePath().normalize();
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.createDirectories(eq(serviceFileStoragePath))).thenReturn(serviceFileStoragePath);

            assertThrows(IllegalArgumentException.class, () -> {
                reviewService.addImagesToReview(1L, images);
            });
            mockedFiles.verify(() -> Files.createDirectories(eq(serviceFileStoragePath)));
        }
    }

    @Test
    void addImagesToReview_NullContentType_ThrowsIllegalArgumentException() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(imageRepository.countByReviewId(1L)).thenReturn(0);
        MockMultipartFile nullContentTypeFile = new MockMultipartFile("images", "image.jpg", null, "content".getBytes());
        List<MultipartFile> images = List.of(nullContentTypeFile);

        Path serviceFileStoragePath = Paths.get("uploads/reviews").toAbsolutePath().normalize();
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.createDirectories(eq(serviceFileStoragePath))).thenReturn(serviceFileStoragePath);

            assertThrows(IllegalArgumentException.class, () -> {
                reviewService.addImagesToReview(1L, images);
            });
            mockedFiles.verify(() -> Files.createDirectories(eq(serviceFileStoragePath)));
        }
    }

    @Test
    void addImagesToReview_StoreImageFails_ThrowsRuntimeException() throws IOException {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(imageRepository.countByReviewId(1L)).thenReturn(0);
        MockMultipartFile imageFile = new MockMultipartFile("images", "image1.jpg", "image/jpeg", "content".getBytes());
        List<MultipartFile> images = List.of(imageFile);

        String fixedUuid = "fixed-uuid-for-store-fail";
        String expectedOriginalFilename = imageFile.getOriginalFilename();
        String expectedStoredFilename = fixedUuid + "_" + expectedOriginalFilename;

        Path serviceFileStoragePath = Paths.get("uploads/reviews").toAbsolutePath().normalize();
        Path expectedTargetLocation = serviceFileStoragePath.resolve(expectedStoredFilename);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class);
             MockedStatic<UUID> mockedUuid = mockStatic(UUID.class)) {

            UUID mockUUIDObj = mock(UUID.class);
            when(mockUUIDObj.toString()).thenReturn(fixedUuid);
            mockedUuid.when(UUID::randomUUID).thenReturn(mockUUIDObj);

            mockedFiles.when(() -> Files.createDirectories(eq(serviceFileStoragePath))).thenReturn(serviceFileStoragePath);
            mockedFiles.when(() -> Files.copy(any(InputStream.class), eq(expectedTargetLocation)))
                       .thenThrow(IOException.class);

            assertThrows(RuntimeException.class, () -> {
                reviewService.addImagesToReview(1L, images);
            });

            mockedFiles.verify(() -> Files.createDirectories(eq(serviceFileStoragePath)));
            mockedUuid.verify(UUID::randomUUID);
            mockedFiles.verify(() -> Files.copy(any(InputStream.class), eq(expectedTargetLocation)));
        }
    }

    @Test
    void addImagesToReview_Success_Refined() throws IOException {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(imageRepository.countByReviewId(1L)).thenReturn(0);
        MockMultipartFile imageFile1 = new MockMultipartFile("images", "image1.jpg", "image/jpeg", "content1".getBytes());
        List<MultipartFile> images = List.of(imageFile1);

        String fixedUuid = "fixed-uuid";
        String expectedOriginalFilename = imageFile1.getOriginalFilename();
        String expectedStoredFilename = fixedUuid + "_" + expectedOriginalFilename;

        Path serviceFileStoragePath = Paths.get("uploads/reviews").toAbsolutePath().normalize();
        Path expectedTargetLocation = serviceFileStoragePath.resolve(expectedStoredFilename);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class);
             MockedStatic<UUID> mockedUuid = mockStatic(UUID.class)) {

            UUID mockUUID = mock(UUID.class);
            when(mockUUID.toString()).thenReturn(fixedUuid);
            mockedUuid.when(UUID::randomUUID).thenReturn(mockUUID);

            mockedFiles.when(() -> Files.createDirectories(eq(serviceFileStoragePath))).thenReturn(serviceFileStoragePath);
            mockedFiles.when(() -> Files.copy(any(InputStream.class), eq(expectedTargetLocation))).thenReturn(1L);

            ReviewImage savedImage = ReviewImage.builder()
                    .fileName(expectedStoredFilename)
                    .filePath(expectedTargetLocation.toString())
                    .contentType(imageFile1.getContentType())
                    .review(review)
                    .build();
            when(imageRepository.save(argThat(img ->
                img.getFileName().equals(expectedStoredFilename) &&
                img.getFilePath().equals(expectedTargetLocation.toString()) &&
                img.getContentType().equals(imageFile1.getContentType()) &&
                img.getReview().getId().equals(review.getId())
            ))).thenReturn(savedImage);

            List<ReviewImage> result = reviewService.addImagesToReview(1L, images);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(expectedStoredFilename, result.get(0).getFileName());
            verify(imageRepository, times(1)).save(any(ReviewImage.class));

            mockedFiles.verify(() -> Files.createDirectories(eq(serviceFileStoragePath)));
            mockedFiles.verify(() -> Files.copy(any(InputStream.class), eq(expectedTargetLocation)));
            mockedUuid.verify(UUID::randomUUID);
        }
    }

    @Test
    void deleteImage_ImageNotFound_ThrowsIllegalArgumentException() {
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        when(imageRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.deleteImage(1L);
        });
    }

    @Test
    void deleteImage_UserNotOwnerNorAdmin_ThrowsAccessDeniedException() {
        User anotherUser = User.builder().id(99L).role(Role.ATTENDEE).build();
        review.setUser(anotherUser);
        ReviewImage image = ReviewImage.builder().id(1L).review(review).filePath("path/to/image.jpg").build();

        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));

        assertThrows(AccessDeniedException.class, () -> {
            reviewService.deleteImage(1L);
        });
    }

    @Test
    void deleteImage_FileDeletionFails_LogsErrorAndContinues() throws IOException {
        ReviewImage image = ReviewImage.builder().id(1L).review(review).filePath("path/to/image.jpg").build();
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));

        Path mockImagePath = mock(Path.class);
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class);
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedPaths.when(() -> Paths.get(image.getFilePath())).thenReturn(mockImagePath);
            mockedFiles.when(() -> Files.deleteIfExists(mockImagePath)).thenThrow(IOException.class);

            assertDoesNotThrow(() -> {
                reviewService.deleteImage(1L);
            });
            verify(imageRepository).delete(image);
        }
    }

    @Test
    void deleteImage_Success_ByUserOwner() throws IOException {
        ReviewImage image = ReviewImage.builder().id(1L).review(review).filePath("path/to/image.jpg").build();
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        doNothing().when(imageRepository).delete(image);

        Path mockImagePath = mock(Path.class);
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class);
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedPaths.when(() -> Paths.get(image.getFilePath())).thenReturn(mockImagePath);
            mockedFiles.when(() -> Files.deleteIfExists(mockImagePath)).thenReturn(true);

            reviewService.deleteImage(1L);

            verify(imageRepository).delete(image);
            mockedFiles.verify(() -> Files.deleteIfExists(mockImagePath));
        }
    }

    @Test
    void deleteImage_Success_ByAdmin() throws IOException {
        User anotherUser = User.builder().id(99L).role(Role.ATTENDEE).build();
        review.setUser(anotherUser);
        ReviewImage image = ReviewImage.builder().id(1L).review(review).filePath("path/to/image.jpg").build();
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        doNothing().when(imageRepository).delete(image);

        Path mockImagePath = mock(Path.class);
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class);
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedPaths.when(() -> Paths.get(image.getFilePath())).thenReturn(mockImagePath);
            mockedFiles.when(() -> Files.deleteIfExists(mockImagePath)).thenReturn(true);

            reviewService.deleteImage(1L);

            verify(imageRepository).delete(image);
            mockedFiles.verify(() -> Files.deleteIfExists(mockImagePath));
        }
    }

    @Test
    void respondToReview_ReviewNotFound_ThrowsIllegalArgumentException() {
        when(authService.getCurrentUser()).thenReturn(organizerUser);
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());
        ReviewResponseRequest request = new ReviewResponseRequest();
        request.setContent("Thanks!");

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.respondToReview(1L, request);
        });
    }

    @Test
    void respondToReview_UserNotOrganizer_ThrowsAccessDeniedException() {
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        ReviewResponseRequest request = new ReviewResponseRequest();
        request.setContent("Thanks!");

        assertThrows(AccessDeniedException.class, () -> {
            reviewService.respondToReview(1L, request);
        });
    }

    @Test
    void respondToReview_Success() {
        when(authService.getCurrentUser()).thenReturn(organizerUser);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        ReviewResponseRequest request = new ReviewResponseRequest();
        request.setContent("Organizer response");

        ReviewResponse expectedResponse = ReviewResponse.builder()
                .content("Organizer response")
                .organizer(organizerUser)
                .review(review)
                .build();
        when(responseRepository.save(any(ReviewResponse.class))).thenReturn(expectedResponse);

        ReviewResponse actualResponse = reviewService.respondToReview(1L, request);

        assertNotNull(actualResponse);
        assertEquals("Organizer response", actualResponse.getContent());
        assertEquals(organizerUser, actualResponse.getOrganizer());
        verify(responseRepository).save(any(ReviewResponse.class));
    }

    @Test
    void reportReview_ReviewNotFound_ThrowsIllegalArgumentException() {
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());
        ReviewReportRequest request = new ReviewReportRequest();
        request.setReason("Spam");

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.reportReview(1L, request);
        });
    }

    @Test
    void reportReview_UserAlreadyReported_ThrowsIllegalArgumentException() {
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reportRepository.existsByReviewIdAndReporterId(1L, attendeeUser.getId())).thenReturn(true);
        ReviewReportRequest request = new ReviewReportRequest();
        request.setReason("Spam");

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.reportReview(1L, request);
        });
    }

    @Test
    void reportReview_Success() {
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reportRepository.existsByReviewIdAndReporterId(1L, attendeeUser.getId())).thenReturn(false);

        ReviewReportRequest request = new ReviewReportRequest();
        request.setReason("Spam content");

        ReviewReport expectedReport = ReviewReport.builder()
                .reason("Spam content")
                .status(ReviewReport.ReportStatus.PENDING)
                .review(review)
                .reporter(attendeeUser)
                .build();
        when(reportRepository.save(any(ReviewReport.class))).thenReturn(expectedReport);
        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        when(reviewRepository.save(reviewCaptor.capture())).thenReturn(review);

        ReviewReport actualReport = reviewService.reportReview(1L, request);

        assertNotNull(actualReport);
        assertEquals("Spam content", actualReport.getReason());
        assertTrue(reviewCaptor.getValue().getIsReported());
        verify(reportRepository).save(any(ReviewReport.class));
        verify(reviewRepository).save(any(Review.class));
        verify(eventPublisher).publishEvent(any(ReviewReportedEvent.class));
    }

    @Test
    void getReportedReviews_UserNotAdmin_ThrowsAccessDeniedException() {
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        assertThrows(AccessDeniedException.class, () -> {
            reviewService.getReportedReviews();
        });
    }

    @Test
    void getReportedReviews_Success() {
        when(authService.getCurrentUser()).thenReturn(adminUser);
        List<Review> reportedReviews = Collections.singletonList(review);
        when(reviewRepository.findByIsReportedTrue()).thenReturn(reportedReviews);

        List<Review> actualReviews = reviewService.getReportedReviews();

        assertEquals(reportedReviews, actualReviews);
        verify(reviewRepository).findByIsReportedTrue();
    }

    @Test
    void approveReport_UserNotAdmin_ThrowsAccessDeniedException() {
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        assertThrows(AccessDeniedException.class, () -> {
            reviewService.approveReport(1L);
        });
    }

    @Test
    void approveReport_ReportNotFound_ThrowsIllegalArgumentException() {
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(reportRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.approveReport(1L);
        });
    }

    @Test
    void approveReport_Success() {
        when(authService.getCurrentUser()).thenReturn(adminUser);
        ReviewReport report = ReviewReport.builder().id(1L).review(review).status(ReviewReport.ReportStatus.PENDING).build();
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        ArgumentCaptor<ReviewReport> reportCaptor = ArgumentCaptor.forClass(ReviewReport.class);
        when(reportRepository.save(reportCaptor.capture())).thenReturn(report);

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        when(reviewRepository.save(reviewCaptor.capture())).thenReturn(review);

        reviewService.approveReport(1L);

        assertEquals(ReviewReport.ReportStatus.APPROVED, reportCaptor.getValue().getStatus());
        assertEquals(adminUser, reportCaptor.getValue().getAdmin());
        assertFalse(reviewCaptor.getValue().getIsVisible());
        verify(reportRepository).save(any(ReviewReport.class));
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void rejectReport_UserNotAdmin_ThrowsAccessDeniedException() {
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        assertThrows(AccessDeniedException.class, () -> {
            reviewService.rejectReport(1L);
        });
    }

    @Test
    void rejectReport_ReportNotFound_ThrowsIllegalArgumentException() {
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(reportRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.rejectReport(1L);
        });
    }

    @Test
    void rejectReport_Success_NoOtherPendingReports() {
        when(authService.getCurrentUser()).thenReturn(adminUser);
        ReviewReport report = ReviewReport.builder().id(1L).review(review).status(ReviewReport.ReportStatus.PENDING).build();
        review.setIsReported(true);

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        ArgumentCaptor<ReviewReport> reportCaptor = ArgumentCaptor.forClass(ReviewReport.class);
        when(reportRepository.save(reportCaptor.capture())).thenReturn(report);

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        when(reviewRepository.save(reviewCaptor.capture())).thenReturn(review);

        when(reportRepository.findByReviewId(review.getId())).thenAnswer(invocation -> {
            ReviewReport currentReport = reportCaptor.getValue();
            return Collections.singletonList(currentReport);
        });

        reviewService.rejectReport(1L);

        assertEquals(ReviewReport.ReportStatus.REJECTED, reportCaptor.getValue().getStatus());
        assertEquals(adminUser, reportCaptor.getValue().getAdmin());
        assertFalse(reviewCaptor.getValue().getIsReported());
        verify(reportRepository).save(any(ReviewReport.class));
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void rejectReport_Success_WithOtherPendingReports() {
        when(authService.getCurrentUser()).thenReturn(adminUser);
        ReviewReport reportToReject = ReviewReport.builder().id(1L).review(review).status(ReviewReport.ReportStatus.PENDING).build();
        ReviewReport otherPendingReport = ReviewReport.builder().id(2L).review(review).status(ReviewReport.ReportStatus.PENDING).build();
        review.setIsReported(true);

        when(reportRepository.findById(1L)).thenReturn(Optional.of(reportToReject));

        ArgumentCaptor<ReviewReport> reportCaptor = ArgumentCaptor.forClass(ReviewReport.class);
        when(reportRepository.save(reportCaptor.capture())).thenReturn(reportToReject);

        List<ReviewReport> reportsForReview = new ArrayList<>();
        reportsForReview.add(reportToReject);
        reportsForReview.add(otherPendingReport);
        when(reportRepository.findByReviewId(review.getId())).thenReturn(reportsForReview);

        reviewService.rejectReport(1L);

        assertEquals(ReviewReport.ReportStatus.REJECTED, reportCaptor.getValue().getStatus());
        assertEquals(adminUser, reportCaptor.getValue().getAdmin());
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void hideReview_UserNotAdmin_ThrowsAccessDeniedException() {
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        assertThrows(AccessDeniedException.class, () -> {
            reviewService.hideReview(1L);
        });
    }

    @Test
    void hideReview_ReviewNotFound_ThrowsIllegalArgumentException() {
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.hideReview(1L);
        });
    }

    @Test
    void hideReview_Success() {
        when(authService.getCurrentUser()).thenReturn(adminUser);
        review.setIsVisible(true);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        when(reviewRepository.save(reviewCaptor.capture())).thenReturn(review);

        reviewService.hideReview(1L);

        assertFalse(reviewCaptor.getValue().getIsVisible());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void restoreReview_UserNotAdmin_ThrowsAccessDeniedException() {
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        assertThrows(AccessDeniedException.class, () -> {
            reviewService.restoreReview(1L);
        });
    }

    @Test
    void restoreReview_ReviewNotFound_ThrowsIllegalArgumentException() {
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.restoreReview(1L);
        });
    }

    @Test
    void restoreReview_Success() {
        when(authService.getCurrentUser()).thenReturn(adminUser);
        review.setIsVisible(false);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        when(reviewRepository.save(reviewCaptor.capture())).thenReturn(review);

        reviewService.restoreReview(1L);

        assertTrue(reviewCaptor.getValue().getIsVisible());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void getReviewsByEventIdPaginated_CallsCorrectRepositoryMethod() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Review> expectedPage = new PageImpl<>(Collections.singletonList(review));
        when(reviewRepository.findByEventIdOrderByCreatedAtDesc(101L, pageable)).thenReturn(expectedPage);

        Page<Review> result = reviewService.getReviewsByEventIdPaginated(101L, pageable);

        assertEquals(expectedPage, result);
        verify(reviewRepository).findByEventIdOrderByCreatedAtDesc(101L, pageable);
    }

    @Test
    void searchReviewsByKeyword_CallsCorrectRepositoryMethod() {
        Pageable pageable = PageRequest.of(0, 5);
        String keyword = "Great";
        Page<Review> expectedPage = new PageImpl<>(Collections.singletonList(review));
        when(reviewRepository.findByEventIdAndContentContaining(101L, keyword, pageable)).thenReturn(expectedPage);

        Page<Review> result = reviewService.searchReviewsByKeyword(101L, keyword, pageable);

        assertEquals(expectedPage, result);
        verify(reviewRepository).findByEventIdAndContentContaining(101L, keyword, pageable);
    }

    @Test
    void getAverageRatingForEvent_CallsRepository() {
        when(reviewRepository.calculateAverageRatingForEvent(101L)).thenReturn(4.5);
        Double avg = reviewService.getAverageRatingForEvent(101L);
        assertEquals(4.5, avg);
        verify(reviewRepository).calculateAverageRatingForEvent(101L);
    }

    @Test
    void hasUserAlreadyReviewed_CallsRepository() {
        when(reviewRepository.existsByUserIdAndEventId(1L, 101L)).thenReturn(true);
        assertTrue(reviewService.hasUserAlreadyReviewed(1L, 101L));
        verify(reviewRepository).existsByUserIdAndEventId(1L, 101L);
    }

    @Test
    void deleteImage_FileDeletionFails_ShouldLogErrorAndContinue() throws IOException {
        when(authService.getCurrentUser()).thenReturn(attendeeUser);
        ReviewImage image = ReviewImage.builder().id(1L).review(review).filePath("path/to/image.jpg").build();
        review.setUser(attendeeUser);
        image.setReview(review);

        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));

        Path mockImagePath = mock(Path.class);
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class);
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {

            mockedPaths.when(() -> Paths.get(image.getFilePath())).thenReturn(mockImagePath);
            mockedFiles.when(() -> Files.deleteIfExists(mockImagePath)).thenThrow(new IOException("Test deletion failure"));

            assertDoesNotThrow(() -> reviewService.deleteImage(1L));

            verify(imageRepository).delete(image);
            mockedFiles.verify(() -> Files.deleteIfExists(mockImagePath));
        }
    }
}
