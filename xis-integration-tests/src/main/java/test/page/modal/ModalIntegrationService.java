package test.page.modal;

import java.util.HashMap;
import java.util.Map;

class ModalIntegrationService {

    private int pageVersion;
    private int modalLoadCount;
    private String modalSource = "";
    private String savedValue = "empty";
    private final Map<String, Integer> cardVersions = new HashMap<>();
    private final Map<String, String> cardValues = new HashMap<>();

    int pageVersion() {
        return pageVersion;
    }

    String savedValue() {
        return savedValue;
    }

    int modalLoadCount() {
        return modalLoadCount;
    }

    String modalSource() {
        return modalSource;
    }

    void modalLoaded(String source) {
        modalSource = source;
        modalLoadCount++;
    }

    void save(String value) {
        savedValue = value;
        pageVersion++;
    }

    String cardValue(String card) {
        return cardValues.getOrDefault(card, "empty");
    }

    int cardVersion(String card) {
        return cardVersions.getOrDefault(card, 0);
    }

    void saveCardValue(String card, String value) {
        cardValues.put(card, value);
        cardVersions.merge(card, 1, Integer::sum);
    }
}
