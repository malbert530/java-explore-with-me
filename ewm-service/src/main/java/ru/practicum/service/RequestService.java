package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Event;
import ru.practicum.model.Request;
import ru.practicum.model.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;
    private final UserService userService;
    private final EventRepository eventRepository;
    private final EventService eventService;

    @Transactional
    public ParticipationRequestDto createNewRequest(Long userId, Long eventId) {
        User requester = userService.getUserIfExistOrElseThrow(userId);

        Event event = eventService.getEventIfExistOrElseThrow(eventId);
        if (!event.getState().equals(Event.State.PUBLISHED)) {
            String errorMessage = String.format("Событие с id = %d недоступно", eventId);
            throw new ValidationException(errorMessage);
        }

        if (Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ValidationException("Инициатор события не может добавить запрос на участие в своём событии");
        }

        Long eventParticipants = requestRepository.countByEventIdAndStatus(eventId, Request.Status.CONFIRMED);
        if (event.getParticipantLimit() > 0 && event.getParticipantLimit() == eventParticipants) {
            throw new ValidationException("У события достигнут лимит запросов на участие");
        }

        Request newRequest = Request.builder()
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))
                .requester(requester)
                .event(event)
                .status(Request.Status.PENDING)
                .build();

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            newRequest.setStatus(Request.Status.CONFIRMED);
        }

        try {
            return RequestMapper.toDto(requestRepository.saveAndFlush(newRequest));
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Нельзя сделать повторный запрос на участие в событии");
        }
    }


    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userService.getUserIfExistOrElseThrow(userId);
        List<Request> allRequests = requestRepository.findAllByRequesterId(userId);
        return allRequests.stream().map(RequestMapper::toDto).toList();
    }

    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        userService.getUserIfExistOrElseThrow(userId);
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id = " + requestId + " не найден"));
        request.setStatus(Request.Status.CANCELED);
        requestRepository.saveAndFlush(request);
        return RequestMapper.toDto(request);
    }
}
