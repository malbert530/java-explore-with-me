package ru.practicum.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Embeddable
public class Location {
    private Double lat;
    private Double lon;
}
