package test.page.storage;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ClientStorageStoreData {
    private final List<String> items = new ArrayList<>();

    void addItem(String item) {
        items.add(item);
    }
}
