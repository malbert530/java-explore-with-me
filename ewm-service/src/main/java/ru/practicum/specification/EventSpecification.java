package ru.practicum.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.dto.event.Sort;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public class EventSpecification {

    public static Specification<Event> users(List<Long> users) {
        return (root, query, criteriaBuilder) ->
                users == null || users.isEmpty() ? criteriaBuilder.conjunction() : root.get("initiator").get("id").in(users);
    }

    public static Specification<Event> categories(List<Long> categories) {
        return (root, query, criteriaBuilder) ->
                categories == null || categories.isEmpty() ? criteriaBuilder.conjunction() : root.get("category").get("id").in(categories);
    }

    public static Specification<Event> states(List<Event.State> states) {
        return (root, query, criteriaBuilder) ->
                states == null || states.isEmpty() ? criteriaBuilder.conjunction() : root.get("state").in(states);
    }


    public static Specification<Event> startAfter(LocalDateTime rangeStart) {
        return (root, query, criteriaBuilder) ->
                rangeStart != null
                        ? criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart)
                        : criteriaBuilder.conjunction();
    }

    public static Specification<Event> startBefore(LocalDateTime rangeEnd) {
        return (root, query, criteriaBuilder) ->
                rangeEnd != null
                        ? criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd)
                        : criteriaBuilder.conjunction();
    }

    public static Specification<Event> searchInAnnotationOrDescription(String text) {
        return (root, query, criteriaBuilder) ->
                text == null || text.isBlank() ? criteriaBuilder.conjunction() :
                        criteriaBuilder.or(
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), "%" + text.toLowerCase() + "%"),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + text.toLowerCase() + "%")
                        );
    }

    public static Specification<Event> isPaid(Boolean paid) {
        return (root, query, criteriaBuilder) ->
                paid == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("paid"), paid);
    }

    public static Specification<Event> eventAdminSearch(List<Long> users, List<Event.State> states, List<Long> categories,
                                                        LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        return Specification.where(users(users))
                .and(states(states))
                .and(categories(categories))
                .and(startBefore(rangeEnd))
                .and(startAfter(rangeStart));
    }

    public static Specification<Event> eventPublicSearch(String text, List<Long> categories, Boolean paid,
                                                         LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                         Sort sort) {
        return Specification.where(searchInAnnotationOrDescription(text))
                .and(categories(categories))
                .and(isPaid(paid))
                .and(startBefore(rangeEnd))
                .and(startAfter(rangeStart))
                .and(states(List.of(Event.State.PUBLISHED)));
    }
}
