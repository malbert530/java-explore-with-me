package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Request;

import java.util.List;
import java.util.Map;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByRequesterId(Long userId);

    Long countByEventIdAndStatus(Long eventId, Request.Status status);

    @Query("SELECT r.event.id, COUNT(r.event.id) " +
            "FROM Request r " +
            "WHERE r.event.id in ?1 AND r.status = ?2 " +
            "GROUP BY r.event.id")
    Map<Long, Long> getEventRequests(List<Long> eventIds, Request.Status status);

    List<Request> findAllByEventIdInAndStatus(List<Long> eventIds, Request.Status status);

    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByIdIn(List<Long> ids);
}
