package test.reactive.buttonlocalstorage;

import lombok.Data;

@Data
public class CounterData {
    private int value;
    
    public CounterData() {
        this.value = 0;
    }
    
    public CounterData(int value) {
        this.value = value;
    }
}