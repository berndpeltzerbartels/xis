package test.page;

import lombok.Data;

@Data
class Holder<T> {
    private T value;
}
