package ru.yandex.practicum.dto;

import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;

@Data
public class PageWithSort<T> {
    private List<T> content;
    private List<Sort.Order> sort;
    private int number;
    private int size;
    private long totalElements;
    private int totalPages;

    public static <T> PageWithSort<T> from(Page<T> page) {
        PageWithSort<T> result = new PageWithSort<>();
        result.setContent(page.getContent());
        result.setSort(page.getSort().toList());
        result.setNumber(page.getNumber());
        result.setSize(page.getSize());
        result.setTotalElements(page.getTotalElements());
        result.setTotalPages(page.getTotalPages());
        return result;
    }
}