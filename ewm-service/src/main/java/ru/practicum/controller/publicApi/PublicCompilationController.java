package ru.practicum.controller.publicApi;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.service.CompilationService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/compilations")
@RequiredArgsConstructor
public class PublicCompilationController {
    private final CompilationService service;

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable @Min(1) Long compId) {
        log.info("Получен запрос на получение подборки с id = {}", compId);
        return service.getCompilationById(compId);
    }

    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(defaultValue = "0") int from,
                                                @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("Получен запрос на получение всех подборок согласно фильтру");
        return service.getCompilations(from, size, pinned);
    }
}
