package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ViewStatsDto;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.repository.CompilationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventService eventService;


    public CompilationDto createNewCompilation(NewCompilationDto dto) {
        List<Event> events = new ArrayList<>();
        if (dto.getEvents() != null) {
            events = eventService.getAllEventsByIds(dto.getEvents());
        }
        Compilation compilation = CompilationMapper.newCompilationToModel(dto, events);

        Compilation savedCompilation = compilationRepository.saveAndFlush(compilation);

        List<Event> allEvents = savedCompilation.getEvents();
        List<ViewStatsDto> stats = eventService.getAllEventsViewStats(allEvents);
        Map<Long, Long> eventRequests = eventService.getEventsConfirmedRequests(allEvents);

        List<EventShortDto> shortEvents = allEvents.stream().map(event -> {
            if (event.getState().equals(Event.State.PUBLISHED)) {
                return eventService.createEventShortDto(event, stats, eventRequests);
            }
            return EventMapper.toShortDto(event, 0L, 0L);
        }).toList();

        return CompilationMapper.toDto(savedCompilation, shortEvents);
    }


    public void deleteCompilation(Long compId) {
        getCompilationIfExistOrElseThrow(compId);
        compilationRepository.deleteById(compId);
    }

    private Compilation getCompilationIfExistOrElseThrow(Long id) {
        return compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Подборка с id = " + id + " не найдена"));
    }

    public CompilationDto updateCompilation(UpdateCompilationRequest dto, Long compId) {
        Compilation oldCompilation = getCompilationIfExistOrElseThrow(compId);
        if (dto.getTitle() != null) {
            oldCompilation.setTitle(dto.getTitle());
        }
        if (dto.getPinned() != null) {
            oldCompilation.setPinned(dto.getPinned());
        }
        if (dto.getEvents() != null) {
            List<Event> events = eventService.getAllEventsByIds(dto.getEvents());
            oldCompilation.setEvents(events);
        }
        Compilation updated = compilationRepository.saveAndFlush(oldCompilation);

        List<Event> allEvents = updated.getEvents();
        List<ViewStatsDto> stats = eventService.getAllEventsViewStats(allEvents);
        Map<Long, Long> eventRequests = eventService.getEventsConfirmedRequests(allEvents);

        List<EventShortDto> shortEvents = allEvents.stream().map(event -> {
            if (event.getState().equals(Event.State.PUBLISHED)) {
                return eventService.createEventShortDto(event, stats, eventRequests);
            }
            return EventMapper.toShortDto(event, 0L, 0L);
        }).toList();

        return CompilationMapper.toDto(updated, shortEvents);
    }

    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = getCompilationIfExistOrElseThrow(compId);
        List<Event> allEvents = compilation.getEvents();

        List<ViewStatsDto> stats = eventService.getAllEventsViewStats(allEvents);
        Map<Long, Long> eventRequests = eventService.getEventsConfirmedRequests(allEvents);

        List<EventShortDto> shortEvents = allEvents.stream().map(event -> {
            if (event.getState().equals(Event.State.PUBLISHED)) {
                return eventService.createEventShortDto(event, stats, eventRequests);
            }
            return EventMapper.toShortDto(event, 0L, 0L);
        }).toList();

        return CompilationMapper.toDto(compilation, shortEvents);
    }

    public List<CompilationDto> getCompilations(int from, int size, Boolean pinned) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Compilation> allCompilations;
        if (pinned != null) {
            allCompilations = compilationRepository.findAllByPinned(pinned, pageable).getContent();
        } else {
            allCompilations = compilationRepository.findAll(pageable).getContent();
        }
        return allCompilations.stream().map(o -> getCompilationById(o.getId())).toList();
    }
}
