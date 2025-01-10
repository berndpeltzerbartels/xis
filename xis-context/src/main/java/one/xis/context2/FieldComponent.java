package one.xis.context2;

class FieldComponent implements SingletonProducer {
    @Override
    public Class<?> getSingletonClass() {
        return null;
    }

    @Override
    public boolean isReadyForProduction() {
        return false;
    }

    @Override
    public void addConsumer(SingletonConsumer consumer) {

    }

    @Override
    public void addListener(SingletonCreationListener listener) {

    }

    @Override
    public void invoke() {

    }
}
