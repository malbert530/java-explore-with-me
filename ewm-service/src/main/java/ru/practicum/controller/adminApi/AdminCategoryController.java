package ru.practicum.controller.adminApi;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.service.CategoryService;

@Validated
@Slf4j
@RestController
@RequestMapping(path = "/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createNewCategory(@Valid @RequestBody NewCategoryDto dto) {
        log.info("Получен запрос на создание новой категории {}", dto);
        return service.createNewCategory(dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable @Min(1) Long id) {
        log.info("Получен запрос на удаление категории с id = {}", id);
        service.deleteCategory(id);
    }

    @PatchMapping("/{id}")
    public CategoryDto updateCategory(@Valid @RequestBody CategoryDto dto,
                                      @PathVariable @Min(1) Long id) {
        log.info("Получен запрос на обновление категории с id = {}", id);
        return service.updateCategory(dto, id);
    }
}
