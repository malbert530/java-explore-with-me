package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository repository;
    private final EventRepository eventRepository;

    @Transactional
    public CategoryDto createNewCategory(NewCategoryDto dto) {
        Category savedCategory = saveCategory(CategoryMapper.toModel(dto));
        log.info("Coздана новая категория с id = {}", savedCategory.getId());
        return CategoryMapper.toDto(savedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category cat = getCategoryIfExistOrElseThrow(id);
        List<Event> eventsInCategory = eventRepository.findByCategoryId(id);

        if (!eventsInCategory.isEmpty()) {
            String errorMessage = "С данной категорией связаны события";
            throw new ValidationException(errorMessage);
        }

        repository.deleteById(id);
        log.info("Удалена категория {}", cat);
    }

    @Transactional
    public CategoryDto updateCategory(CategoryDto dto, Long id) {
        Category oldCategory = getCategoryIfExistOrElseThrow(id);
        oldCategory.setName(dto.getName());
        Category updated = saveCategory(oldCategory);
        log.info("Категория с id = {} обновлена", id);
        return CategoryMapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public CategoryDto getById(Long id) {
        Category category = getCategoryIfExistOrElseThrow(id);
        return CategoryMapper.toDto(category);
    }

    public Category getCategoryIfExistOrElseThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория с id = " + id + " не найдена"));
    }

    private Category saveCategory(Category category) {
        try {
            return repository.saveAndFlush(category);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = String.format("Категория с названием %s уже существует", category.getName());
            throw new ValidationException(errorMessage);
        }
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return repository.findAll(pageable).getContent().stream().map(CategoryMapper::toDto).toList();
    }

}
