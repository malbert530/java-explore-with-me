package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;

    @Transactional
    public UserDto createNewUser(NewUserRequest dto) {
        User savedUser = saveUser(UserMapper.toModel(dto));
        log.info("Coздан новый пользователь с id = {}", savedUser.getId());
        return UserMapper.toUserDto(savedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserIfExistOrElseThrow(id);
        repository.deleteById(id);
        log.info("Удален пользователь {}", user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers(int from, int size, List<Long> ids) {
        Pageable pageable = PageRequest.of(from / size, size);
        if (ids == null || ids.isEmpty()) {
            return repository.findAll(pageable).getContent().stream().map(UserMapper::toUserDto).toList();
        } else {
            return repository.findByIdIn(ids, pageable).getContent().stream().map(UserMapper::toUserDto).toList();
        }
    }

    public User getUserIfExistOrElseThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

    private User saveUser(User user) {
        try {
            return repository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = String.format("Пользователь с email %s уже существует", user.getEmail());
            throw new ValidationException(errorMessage);
        }
    }
}
