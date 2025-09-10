package ru.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatsService {
    private final StatsRepository repository;

    public void createNewEndpointHit(EndpointHitDto dto) {
        EndpointHit endpointHit = EndpointHitMapper.toModel(dto);
        EndpointHit saved = repository.saveAndFlush(endpointHit);
        log.info("Запись сохранена как {}", saved);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, boolean unique, List<String> uris) {
        if (start.isAfter(end)) {
            String errorMessage = "Время начала start должно быть раньше, чем время окончания end";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
        List<ViewStatsDto> statsList;
        if (uris == null || uris.isEmpty()) {
            statsList = unique ? repository.getStatsUniqueIp(start, end) : repository.getStats(start, end);
        } else {
            statsList = unique ? repository.getStatsUniqueIpForUris(start, end, uris) : repository.getStatsForUris(start, end, uris);
        }
        return statsList;
    }
}
