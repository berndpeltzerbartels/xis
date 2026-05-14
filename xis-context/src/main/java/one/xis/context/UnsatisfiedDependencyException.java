package one.xis.context;

class UnsatisfiedDependencyException extends RuntimeException {

    public UnsatisfiedDependencyException(SingletonConsumer consumer) {
        super(errorText(consumer));
    }

    private static String errorText(SingletonConsumer consumer) {
        var builder = new StringBuilder("Unsatisfied dependencies: ");
        if (consumer instanceof SimpleDependencyField simpleDependencyField) {
            builder.append("no candidate of type '")
                    .append(simpleDependencyField.getConsumedClass().getSimpleName())
                    .append(" for field ")
                    .append(simpleDependencyField.getField().getName())
                    .append(" in bean ")
                    .append(simpleDependencyField.getField().getDeclaringClass().getSimpleName());

        } else if (consumer instanceof SimpleParam simpleParam) {
            builder.append("no candidate of type '")
                    .append(simpleParam.getConsumedClass().getSimpleName())
                    .append("' for parameter with index ")
                    .append(simpleParam.getIndex());
            if (simpleParam.getParentProducer() != null) {
                if (simpleParam.getParentProducer() instanceof SingletonConstructor constructor) {
                    builder.append(" in constructor of bean ")
                            .append(constructor.getSingletonClass().getSimpleName());
                } else if (simpleParam.getParentProducer() instanceof SingletonMethod method) {
                    builder.append(" in method ")
                            .append(method.getMethod().getName())
                            .append(" of bean ")
                            .append(method.getSingletonClass().getSimpleName());
                } else {
                    builder.append(" in bean ")
                            .append(simpleParam.getParentProducer().getSingletonClass().getSimpleName());
                }
            }
        } else {
            builder.append("no candidate for consumer ")
                    .append(consumer.getClass().getSimpleName());
        }

        return builder.toString();
    }
}
