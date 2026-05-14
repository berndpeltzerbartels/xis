package test.page.modal;

class ModalIntegrationService {

    private int pageVersion;
    private int modalLoadCount;
    private String modalSource = "";
    private String savedValue = "empty";

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
}
