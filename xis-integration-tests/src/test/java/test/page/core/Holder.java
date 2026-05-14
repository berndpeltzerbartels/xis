package test.page.core;

import lombok.Data;

@Data
class Holder<T> {
    private T value;
}
