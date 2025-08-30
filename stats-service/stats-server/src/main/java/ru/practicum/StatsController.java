package ru.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService service;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void createNewEndpointHit(@RequestBody EndpointHitDto dto) {
        log.info("Получен запрос на создание записи {}", dto);
        service.createNewEndpointHit(dto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getAllStats(@RequestParam LocalDateTime start,
                                          @RequestParam LocalDateTime end,
                                          @RequestParam(defaultValue = "false") Boolean unique,
                                          @RequestParam(required = false) List<String> uris) {
        log.info("Получен запрос на получение статистики c {} по {}, для уникальных ip - {}, для uri = {}",
                start, end, unique, uris);
        return service.getStats(start, end, unique, uris);
    }
}
