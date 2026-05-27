package test.page.storage;

public class MethodStorageData {
    private String kept;
    private String removed;

    public MethodStorageData() {
    }

    MethodStorageData(String kept, String removed) {
        this.kept = kept;
        this.removed = removed;
    }

    public String getKept() {
        return kept;
    }

    public void setKept(String kept) {
        this.kept = kept;
    }

    public String getRemoved() {
        return removed;
    }

    public void setRemoved(String removed) {
        this.removed = removed;
    }
}
