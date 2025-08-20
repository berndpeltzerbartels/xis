package test.page.forms.checkbox;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.xis.Action;
import one.xis.FormData;
import one.xis.HtmlFile;
import one.xis.Page;
import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheckboxCompatibilityTest {


    @Nested
    @DisplayName("A field value in form model is true causes the checkbox to be unchecked")
    class TrueValueCheckboxTest {
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        static class CheckboxFormModel {
            private Boolean accepted;
        }

        @Getter
        @HtmlFile("/TestCheckboxPage.html")
        @Page("/checkbox.html")
        static class CheckboxPage {
            private CheckboxFormModel checkboxFormModel = new CheckboxFormModel(true); // should be unchecked

            @FormData("formData")
            CheckboxFormModel formData() {
                return checkboxFormModel;
            }

            @Action
            void submit(@FormData("formData") CheckboxFormModel formData) {
                this.checkboxFormModel = formData;
            }

        }

        private IntegrationTestContext context;

        @BeforeEach
        void init() {
            context = IntegrationTestContext.builder()
                    .withSingleton(CheckboxPage.class)
                    .build();

        }

        @Test
        void testCheckboxCheckedWhenValueIsTrue() {
            var result = context.openPage(CheckboxPage.class);
            var checkbox = result.getDocument().getInputElementById("theCheckbox");
            assertThat(checkbox.isChecked()).isTrue();
        }

    }

    @Nested
    @DisplayName("A field value in form model is null causes the checkbox to be unchecked")
    class NullValueCheckboxTest {

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        static class CheckboxFormModel {
            private Boolean accepted;
        }

        @Getter
        @HtmlFile("/TestCheckboxPage.html")
        @Page("/checkbox.html")
        static class CheckboxPage {
            private CheckboxFormModel checkboxFormModel = new CheckboxFormModel(null); // should be unchecked

            @FormData("formData")
            CheckboxFormModel formData() {
                return checkboxFormModel;
            }

            @Action
            void submit(@FormData("formData") CheckboxFormModel formData) {
                this.checkboxFormModel = formData;
            }

        }

        private IntegrationTestContext context;

        @BeforeEach
        void init() {
            context = IntegrationTestContext.builder()
                    .withSingleton(CheckboxPage.class)
                    .build();
        }

        @Test
        void testCheckboxUncheckedWhenValueIsNull() {
            var result = context.openPage(CheckboxPage.class);
            var checkbox = result.getDocument().getInputElementById("theCheckbox");

            assertThat(checkbox.isChecked()).isFalse();
        }

    }

    @Nested
    @DisplayName("A field value in form model is not null causes the checkbox to be checked")
    class NotNullValueCheckboxTest {
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        static class CheckboxFormModel {
            private String accepted;
        }

        @Getter
        @HtmlFile("/TestCheckboxPage.html")
        @Page("/checkbox.html")
        static class CheckboxPage {
            private CheckboxFormModel checkboxFormModel = new CheckboxFormModel("bla"); // should be unchecked

            @FormData("formData")
            CheckboxFormModel formData() {
                return checkboxFormModel;
            }

            @Action
            void submit(@FormData("formData") CheckboxFormModel formData) {
                this.checkboxFormModel = formData;
            }

        }

        private IntegrationTestContext context;

        @BeforeEach
        void init() {
            context = IntegrationTestContext.builder()
                    .withSingleton(CheckboxPage.class)
                    .build();
        }

        @Test
        void testCheckboxUncheckedWhenValueIsNull() {
            var result = context.openPage(CheckboxPage.class);
            var checkbox = result.getDocument().getInputElementById("theCheckbox");

            assertThat(checkbox.isChecked()).isTrue();
        }


    }

    @Nested
    class NotEqualValues {

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        static class CheckboxFormModel {
            private String accepted;
        }

        @Getter
        @HtmlFile("/TestCheckboxPageWithValue.html")
        @Page("/checkbox.html")
        static class CheckboxPage {
            private CheckboxFormModel checkboxFormModel = new CheckboxFormModel("101"); // should be unchecked

            @FormData("formData")
            CheckboxFormModel formData() {
                return checkboxFormModel;
            }

            @Action
            void submit(@FormData("formData") CheckboxFormModel formData) {
                this.checkboxFormModel = formData;
            }

        }


        private IntegrationTestContext context;

        @BeforeEach
        void init() {
            context = IntegrationTestContext.builder()
                    .withSingleton(CheckboxPage.class)
                    .build();
        }

        @Test
        void testCheckboxUncheckedWhenValueIsNotEqual() {
            var result = context.openPage(CheckboxPage.class);
            var checkbox = result.getDocument().getInputElementById("theCheckbox");

            assertThat(checkbox.isChecked()).isFalse();
        }
    }
}
