# Advanced Programming Group Project 2024/2025 - A11
# EventSphere - Review & Helpdesk Management Module

## Review Management Architecture Overview

This module implements a feature-focused structure following SOLID principles and using the Observer pattern for event handling. The review system allows users to create reviews for events, calculates average ratings, and notifies relevant components about new reviews.

## Package Structure

The review module follows a package-by-feature approach:

```
id.ac.ui.cs.advprog.eventsphere/
└── review/
    ├── controller/       # REST endpoints for review operations
    ├── service/          # Business logic for review functionality
    ├── repository/       # Data access for review persistence
    ├── model/            # Domain entities
    ├── dto/              # Data Transfer Objects
    ├── event/            # Event objects for the Observer pattern
    └── listener/         # Event listeners implementing the Observer pattern
```

## Architecture Layers

### Controllers Layer

- **ReviewController**: Handles REST API endpoints for creating reviews, retrieving reviews by event, and getting average ratings.

### Service Layer

- **ReviewService**: Interface defining the contract for review operations
- **ReviewServiceImpl**: Implementation that integrates with authentication and publishes events

### Repository Layer

- **ReviewRepository**: JPA repository with custom query for calculating average ratings

### Model/Entity Layer

- **Review**: Entity representing a user review with rating, content, user reference, and event ID

### Event System (Observer Pattern)

- **ReviewCreatedEvent**: Event published when a review is created
- **EventRatingUpdateListener**: Listener that recalculates and updates event ratings
- **ReviewNotificationListener**: Listener that sends notifications about new reviews

## Design Patterns Used

### Observer Pattern

The Observer pattern is the core design pattern used in the Review module, implemented using Spring's event mechanism:

1. **Subject (Publisher)**: The `ReviewServiceImpl` publishes `ReviewCreatedEvent` when a new review is created
2. **Observers (Listeners)**:
    - `EventRatingUpdateListener`: Recalculates average event ratings
    - `ReviewNotificationListener`: Sends notifications about new reviews

### DTO Pattern

- Clean separation between API contracts and internal domain models
- **ReviewRequest**: Validates incoming review data with constraints

### Repository Pattern

- Abstracts data access through the Spring Data JPA repository
- Includes custom query methods for specialized data retrieval

### Container Diagram

![Container Diagram](image/container_diagram_review.png)

### Class Diagram

![Class Diagram](image/class_diagram_review.png)

### Sequence Diagram: User Creates a Review

![Sequence Diagram](image/sequence_diagram_review_1.png)

### Sequence Diagram: Organizer Responds to a Review

![Sequence Diagram](image/sequence_diagram_review_2.png)

### Sequence Diagram: Admin Reviews Reports (Report/Restore)

![Sequence Diagram](image/sequence_diagram_review_3.png)

## Conclusion

The Review module demonstrates a well-structured, maintainable design using the Observer pattern to handle complex interactions between components while maintaining loose coupling. This approach enables the system to evolve with new requirements while minimizing changes to existing code.
