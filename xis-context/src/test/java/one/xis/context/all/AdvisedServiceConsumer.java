package one.xis.context.all;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.Component;

@Getter
@Component
@RequiredArgsConstructor
class AdvisedServiceConsumer {
    private final AdvisedService advisedService;

    String describe() {
        return advisedService.describe();
    }
}
