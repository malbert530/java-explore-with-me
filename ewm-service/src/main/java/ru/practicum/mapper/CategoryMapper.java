package ru.practicum.mapper;

import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.model.Category;

public class CategoryMapper {
    public static Category toModel(NewCategoryDto dto) {
        return Category.builder().name(dto.getName()).build();
    }

    public static CategoryDto toDto(Category category) {
        return CategoryDto.builder().name(category.getName()).id(category.getId()).build();
    }
}
