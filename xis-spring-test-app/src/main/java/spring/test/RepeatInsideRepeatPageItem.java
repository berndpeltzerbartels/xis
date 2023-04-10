package spring.test;

import lombok.Data;

import java.util.List;

@Data
class RepeatInsideRepeatPageItem {
    private final String title;
    private final List<RepeatInsideRepeatPageSubItem> subItems;
}
