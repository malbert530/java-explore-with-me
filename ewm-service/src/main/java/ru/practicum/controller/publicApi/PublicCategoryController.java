package ru.practicum.controller.publicApi;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.service.CategoryService;

import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping(path = "/categories")
@RequiredArgsConstructor
public class PublicCategoryController {
    private final CategoryService service;

    @GetMapping("/{id}")
    public CategoryDto getCategoryById(@PathVariable @Min(1) Long id) {
        log.info("Получен запрос на получение категории с id = {}", id);
        return service.getById(id);
    }

    @GetMapping
    public List<CategoryDto> getAllCategories(@RequestParam(defaultValue = "0") int from,
                                              @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("Получен запрос на получение всех категорий с from = {}, size = {}", from, size);
        return service.getAllCategories(from, size);
    }
}
