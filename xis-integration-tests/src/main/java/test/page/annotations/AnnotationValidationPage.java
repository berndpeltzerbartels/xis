package test.page.annotations;

import lombok.Getter;
import one.xis.Action;
import one.xis.ActionParameter;
import one.xis.FormData;
import one.xis.Formatter;
import one.xis.ModelData;
import one.xis.NullAllowed;
import one.xis.Page;
import one.xis.UseFormatter;
import one.xis.validation.AllElementsMandatory;
import one.xis.validation.EMail;
import one.xis.validation.MinLength;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Page("/annotation-validation.html")
class AnnotationValidationPage {

    @Getter
    private AnnotationForm savedForm;

    @Action("save")
    void save(@FormData("form") AnnotationForm form) {
        savedForm = form;
    }

    @Action("optional")
    @ModelData("optionalResult")
    String optional(@ActionParameter("optional") @NullAllowed Integer optional) {
        return optional == null ? "missing" : String.valueOf(optional);
    }

    record AnnotationForm(
            @UseFormatter(GermanDateFormatter.class)
            LocalDate dueDate,

            @EMail
            String email,

            @MinLength(3)
            String name,

            @AllElementsMandatory
            List<Integer> numbers
    ) {
    }

    public static class GermanDateFormatter implements Formatter<LocalDate> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        @Override
        public String format(LocalDate value, Locale locale, ZoneId zoneId) {
            return value.format(FORMATTER);
        }

        @Override
        public LocalDate parse(String value, Locale locale, ZoneId zoneId) {
            return LocalDate.parse(value, FORMATTER);
        }
    }
}
