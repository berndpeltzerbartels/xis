package one.xis.context.all;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.Component;

@Getter
@Component
@RequiredArgsConstructor
class AdvisedBeanConsumer {
    private final AdvisedBeanService advisedBeanService;

    String describe() {
        return advisedBeanService.describe();
    }
}
