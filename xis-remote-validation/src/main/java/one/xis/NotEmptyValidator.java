package one.xis;

class NotEmptyValidator implements Validator<String, NotEmpty> {
    @Override
    public boolean validate(String value, NotEmpty notEmpty) {
        if (value == null) {
            return false;
        }
        return value.trim().isEmpty();
    }
}
