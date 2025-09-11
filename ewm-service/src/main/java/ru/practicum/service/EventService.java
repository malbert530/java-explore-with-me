package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Request;
import ru.practicum.model.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.specification.EventSpecification;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRep;
    private final UserService userService;
    private final CategoryService categoryService;
    private final StatsClient statsClient;
    private final RequestRepository requestRepository;

    @Transactional
    public EventFullDto createNewEvent(NewEventDto dto, Long userId) {
        User initiator = userService.getUserIfExistOrElseThrow(userId);
        Category category = categoryService.getCategoryIfExistOrElseThrow(dto.getCategory());
        if (dto.getLocation().getLat() == null || dto.getLocation().getLon() == null) {
            String errorMessage = "Данные локации не могут быть null";
            throw new ValidationException(errorMessage);
        }
        validateEventDate(dto.getEventDate());

        Event savedEvent = eventRep.saveAndFlush(EventMapper.toModel(dto, initiator, category));

        return EventMapper.toFullDto(savedEvent, 0L, 0L);
    }

    @Transactional(readOnly = true)
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        User user = userService.getUserIfExistOrElseThrow(userId);
        Event event = getEventIfExistOrElseThrow(eventId);
        validateInitiator(event, user);
        Long views = 0L;
        Long confirmedRequests = 0L;
        if (event.getState().equals(Event.State.PUBLISHED)) {
            views = getEventViews(event);
            confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, Request.Status.CONFIRMED);
        }
        return EventMapper.toFullDto(event, views, confirmedRequests);
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getAllUserEvents(int from, int size, Long userId) {
        Pageable pageable = PageRequest.of(from / size, size);
        userService.getUserIfExistOrElseThrow(userId);
        List<Event> allEvents = eventRep.findByInitiatorId(userId, pageable).getContent();

        List<ViewStatsDto> stats = getAllEventsViewStats(allEvents);

        Map<Long, Long> eventRequests = getEventsConfirmedRequests(allEvents);

        return allEvents.stream().map(event -> {
            if (event.getState().equals(Event.State.PUBLISHED)) {
                return createEventShortDto(event, stats, eventRequests);
            }
            return EventMapper.toShortDto(event, 0L, 0L);
        }).toList();
    }

    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest request) {
        User user = userService.getUserIfExistOrElseThrow(userId);
        Event event = getEventIfExistOrElseThrow(eventId);
        validateInitiator(event, user);
        if (event.getState().equals(Event.State.PUBLISHED)) {
            String errorMessage = "Нельзя изменить опубликованные события";
            throw new ValidationException(errorMessage);
        }
        if (request.hasEventDate()) {
            validateEventDate(request.getEventDate());
        }
        if (request.hasStateAction() && request.getStateAction().equals(StateAction.SEND_TO_REVIEW)) {
            event.setState(Event.State.PENDING);
        } else if (request.hasStateAction() && request.getStateAction().equals(StateAction.CANCEL_REVIEW)) {
            event.setState(Event.State.CANCELED);
        }
        updateEventFields(request, event);
        Event updatedEvent = eventRep.saveAndFlush(event);

        return EventMapper.toFullDto(updatedEvent, 0L, 0L);
    }

    private <T extends BaseEventRequest> void updateEventFields(T request, Event event) {
        if (request.hasAnnotation()) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.hasCategory() && !Objects.equals(request.getCategory(), event.getCategory().getId())) {
            Category category = categoryService.getCategoryIfExistOrElseThrow(request.getCategory());
            event.setCategory(category);
        }
        if (request.hasDescription()) {
            event.setDescription(request.getDescription());
        }
        if (request.hasEventDate()) {
            event.setEventDate(request.getEventDate());
        }
        if (request.hasLocation() && request.getLocation().getLon() != null && request.getLocation().getLat() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.hasPaid()) {
            event.setPaid(request.getPaid());
        }
        if (request.hasParticipantLimit()) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.hasRequestModeration()) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.hasTitle()) {
            event.setTitle(request.getTitle());
        }
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            String errorMessage = "Дата и время, на которые намечено событие не может быть раньше, чем через два часа от текущего момента";
            throw new BadRequestException(errorMessage);
        }
    }

    private void validateInitiator(Event event, User user) {
        if (!event.getInitiator().equals(user)) {
            String errorMessage = String.format("Пользователь с id = %d не является инициатором события с id = %d", user.getId(), event.getId());
            throw new ValidationException(errorMessage);
        }
    }

    @Transactional(readOnly = true)
    public Event getEventIfExistOrElseThrow(Long id) {
        return eventRep.findById(id).orElseThrow(() -> new NotFoundException("Событие с id = " + id + " не найдено"));
    }

    public Long getEventViews(Event event) {
        String uri = ("/events/" + event.getId());
        List<ViewStatsDto> stats = statsClient.getStats(event.getCreatedOn(), LocalDateTime.now(), true, List.of(uri));
        return !stats.isEmpty() ? stats.getFirst().getHits() : 0L;
    }

    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = getEventIfExistOrElseThrow(eventId);
        if (!event.getState().equals(Event.State.PENDING)) {
            String errorMessage = "Нельзя изменить опубликованные или отмененные события";
            throw new ValidationException(errorMessage);
        }
        if (request.hasEventDate() && request.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            String errorMessage = "Дата начала изменяемого события должна быть не ранее чем за час от даты публикации";
            throw new BadRequestException(errorMessage);
        }
        if (request.hasStateAction() && request.getStateAction().equals(StateAction.PUBLISH_EVENT)) {
            event.setState(Event.State.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        } else if (request.hasStateAction() && request.getStateAction().equals(StateAction.REJECT_EVENT)) {
            event.setState(Event.State.CANCELED);
        }
        updateEventFields(request, event);
        Event updatedEvent = eventRep.saveAndFlush(event);

        return EventMapper.toFullDto(updatedEvent, 0L, 0L);
    }

    @Transactional(readOnly = true)
    public List<EventFullDto> getAllEventsByAdmin(List<Long> users, List<Event.State> states, List<Long> categories,
                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            String errorMessage = "Время начала rangeStart должно быть раньше, чем время окончания rangeEnd";
            throw new BadRequestException(errorMessage);
        }
        Specification<Event> eventSpecification = EventSpecification.eventAdminSearch(users, states, categories, rangeStart, rangeEnd);
        List<Event> allEvents = eventRep.findAll(eventSpecification, pageable).getContent();

        List<ViewStatsDto> stats = getAllEventsViewStats(allEvents);

        Map<Long, Long> eventRequests = getEventsConfirmedRequests(allEvents);


        return allEvents.stream().map(event -> event.getState().equals(Event.State.PUBLISHED) ? createEventFullDto(event, stats, eventRequests)
                : EventMapper.toFullDto(event, 0L, 0L)).toList();
    }

    public Map<Long, Long> getEventsConfirmedRequests(List<Event> allEvents) {
        List<Long> eventIds = allEvents.stream().map(Event::getId).toList();
        return requestRepository.findAllByEventIdInAndStatus(eventIds, Request.Status.CONFIRMED)
                .stream().collect(Collectors.groupingBy(r -> r.getEvent().getId(), Collectors.counting()));

    }

    public List<ViewStatsDto> getAllEventsViewStats(List<Event> allEvents) {
        List<String> uris = allEvents.stream().map(event -> "/events/" + event.getId()).toList();
        LocalDateTime start = allEvents.stream().map(Event::getCreatedOn).min(Comparator.naturalOrder()).orElse(LocalDateTime.now());
        LocalDateTime end = LocalDateTime.now().plusMinutes(1);

        return statsClient.getStats(start, end, true, uris);
    }

    @Transactional
    public EventFullDto getPublishedEventById(Long eventId, HttpServletRequest request) {
        Event event = getPublishedEventOrElseThrow(eventId);

        EndpointHitDto hitDto = EndpointHitDto.builder()
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .app("ewm-service")
                .build();

        statsClient.hit(hitDto);

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, Request.Status.CONFIRMED);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        Long eventViews = getEventViews(event);
        return EventMapper.toFullDto(event, eventViews, confirmedRequests);
    }


    @Transactional(readOnly = true)
    public List<EventShortDto> getAllEvents(String text, List<Long> categories, Boolean paid,
                                            LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                            Boolean onlyAvailable, Sort sort, int from,
                                            int size, HttpServletRequest request) {

        Pageable pageable = PageRequest.of(from / size, size);
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            String errorMessage = "Время начала rangeStart должно быть раньше, чем время окончания rangeEnd";
            throw new BadRequestException(errorMessage);
        }

        Specification<Event> eventSpecification = EventSpecification.eventPublicSearch(text, categories, paid, rangeStart, rangeEnd, sort);
        List<Event> allEvents = eventRep.findAll(eventSpecification, pageable).getContent();

        EndpointHitDto hitDto = EndpointHitDto.builder()
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .app("ewm-service")
                .build();
        statsClient.hit(hitDto);

        List<ViewStatsDto> stats = getAllEventsViewStats(allEvents);

        Map<Long, Long> eventRequests = getEventsConfirmedRequests(allEvents);

        Comparator<EventShortDto> comparator = Comparator.comparing(EventShortDto::getId);

        if (Sort.VIEWS.equals(sort)) {
            comparator = Comparator.comparing(EventShortDto::getViews).reversed();
        } else if (Sort.EVENT_DATE.equals(sort)) {
            comparator = Comparator.comparing(EventShortDto::getEventDate);
        }

        return allEvents.stream()
                .filter(event -> !onlyAvailable || event.getParticipantLimit() == 0 || event.getParticipantLimit() > eventRequests.get(event.getId()))
                .map(event -> createEventShortDto(event, stats, eventRequests))
                .sorted(comparator)
                .toList();
    }

    public EventShortDto createEventShortDto(Event event, List<ViewStatsDto> stats, Map<Long, Long> eventRequests) {
        Long views = stats.stream()
                .filter(stat -> event.getId().equals(Long.parseLong(stat.getUri().substring("/events/".length()))))
                .map(ViewStatsDto::getHits)
                .findFirst()
                .orElse(0L);

        long confirmedRequests = eventRequests.getOrDefault(event.getId(), 0L);

        return EventMapper.toShortDto(event, views, confirmedRequests);
    }

    private EventFullDto createEventFullDto(Event event, List<ViewStatsDto> stats, Map<Long, Long> eventRequests) {
        Long views = stats.stream()
                .filter(stat -> event.getId().equals(Long.parseLong(stat.getUri().substring("/events/".length()))))
                .map(ViewStatsDto::getHits)
                .findFirst()
                .orElse(0L);


        long confirmedRequests = eventRequests.getOrDefault(event.getId(), 0L);

        return EventMapper.toFullDto(event, views, confirmedRequests);
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserEventRequests(Long userId, Long eventId) {
        User initiator = userService.getUserIfExistOrElseThrow(userId);
        Event event = getEventIfExistOrElseThrow(eventId);
        validateInitiator(event, initiator);
        List<Request> allRequestsByEventId = requestRepository.findAllByEventId(eventId);
        return allRequestsByEventId.stream().map(RequestMapper::toDto).toList();
    }

    @Transactional
    public EventRequestStatusUpdateResult updateUserEventRequests(Long userId, Long eventId,
                                                                  EventRequestStatusUpdateRequest updateStatusDto) {
        User initiator = userService.getUserIfExistOrElseThrow(userId);
        Event event = getEventIfExistOrElseThrow(eventId);
        validateInitiator(event, initiator);

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, Request.Status.CONFIRMED);

        List<Request> allRequests = requestRepository.findAllByIdIn(updateStatusDto.getRequestIds());

        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() == confirmedRequests && updateStatusDto.getStatus().equals(Request.Status.CONFIRMED)) {
            String errorMessage = "Уже достигнут лимит по заявкам на данное событие";
            throw new ValidationException(errorMessage);
        }

        for (Request req : allRequests) {
            if (!req.getStatus().equals(Request.Status.PENDING)) {
                String errorMessage = String.format("Статус можно изменить только у заявок, находящихся в состоянии ожидания. Заявка с id = %d", req.getId());
                throw new ValidationException(errorMessage);
            }
        }

        EventRequestStatusUpdateResult result = EventRequestStatusUpdateResult.builder().build();

        if (updateStatusDto.getStatus().equals(Request.Status.REJECTED)) {
            List<Request> rejectedRequests = allRequests.stream().peek(r -> r.setStatus(Request.Status.REJECTED)).toList();
            requestRepository.saveAllAndFlush(rejectedRequests);
            result.setRejectedRequests(rejectedRequests.stream().map(RequestMapper::toDto).toList());
        }

        if (updateStatusDto.getStatus().equals(Request.Status.CONFIRMED)) {
            List<Request> confirmed = new ArrayList<>();
            List<Request> rejected = new ArrayList<>();

            long acceptedLimit = event.getParticipantLimit() - confirmedRequests;
            for (int i = 0; i < allRequests.size(); i++, acceptedLimit--) {
                Request request = allRequests.get(i);
                if (acceptedLimit > 0) {
                    request.setStatus(Request.Status.CONFIRMED);
                    confirmed.add(request);
                } else {
                    request.setStatus(Request.Status.REJECTED);
                    rejected.add(request);
                }
            }
            requestRepository.saveAllAndFlush(confirmed);
            if (!rejected.isEmpty()) {
                requestRepository.saveAllAndFlush(rejected);
            }
            result.setConfirmedRequests(confirmed.stream().map(RequestMapper::toDto).toList());
            result.setRejectedRequests(rejected.stream().map(RequestMapper::toDto).toList());
        }

        return result;
    }

    public List<Event> getAllEventsByIds(List<Long> ids) {
        return eventRep.findAllById(ids);
    }

    public Event getPublishedEventOrElseThrow(Long id) {
        return eventRep.findByIdAndState(id, Event.State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + id + " не найдено или недоступно"));
    }
}
