package one.xis.parameter;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import one.xis.validation.ValidatorResultElement;

import java.time.ZoneId;
import java.util.Locale;

@Data
@RequiredArgsConstructor
class ParameterDeserializationContext {
    private final ValidatorResultElement validatorResultElement;
    private final Locale locale;
    private final ZoneId zoneId;

    ParameterDeserializationContext(ValidatorResultElement validatorResultElement, ParameterDeserializationContext parent) {
        this.validatorResultElement = validatorResultElement;
        this.locale = parent.getLocale();
        this.zoneId = parent.getZoneId();
    }
}
