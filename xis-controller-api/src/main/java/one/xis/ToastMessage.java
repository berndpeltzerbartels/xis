package one.xis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ToastMessage {
    private final String message;
    private final ToastLevel level;
}
