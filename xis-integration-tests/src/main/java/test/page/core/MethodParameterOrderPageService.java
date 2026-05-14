package test.page.core;

import java.util.ArrayList;
import java.util.List;

class MethodParameterOrderPageService {
    private final Recorder recorder = new Recorder();

    void record(String msg) {
        recorder.record(msg);
    }

    Recorder getRecorder() {
        return recorder;
    }

    static class Recorder {
        private final List<String> calls = new ArrayList<>();

        void record(String msg) {
            calls.add(msg);
        }

        List<String> getCalls() {
            return calls;
        }
    }

}