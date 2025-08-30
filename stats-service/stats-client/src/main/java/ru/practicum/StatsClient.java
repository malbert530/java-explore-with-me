package ru.practicum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class StatsClient {
    private final RestTemplate rest;
    @Value("${stats-server.url}")
    private String SERVER_URL;
    private final String HIT_API_PREFIX = "/hit";
    private final String STATS_API_PREFIX = "/stats";

    public void hit(EndpointHitDto body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EndpointHitDto> requestEntity = new HttpEntity<>(body, headers);
        rest.postForEntity(SERVER_URL + HIT_API_PREFIX, requestEntity, Void.class);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, Boolean unique, List<String> uris) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<ViewStatsDto> requestEntity = new HttpEntity<>(null, headers);
        StringBuilder path = new StringBuilder(SERVER_URL + STATS_API_PREFIX)
                .append("?start=").append(start)
                .append("&end=").append(end)
                .append("&unique=").append(unique);
        if (uris != null && !uris.isEmpty()) {
            path.append("&uris=").append(String.join(",", uris));
        }
        ViewStatsDto[] body = rest.getForEntity(path.toString(), ViewStatsDto[].class, requestEntity).getBody();
        return body == null ? new ArrayList<>() : List.of(body);
    }
}

