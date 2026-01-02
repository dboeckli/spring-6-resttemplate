package guru.springframework.spring6resttemplate.dto;

public record PageMeta(Integer size,
                       Integer number,
                       Long totalElements,
                       Integer totalPages) {}

