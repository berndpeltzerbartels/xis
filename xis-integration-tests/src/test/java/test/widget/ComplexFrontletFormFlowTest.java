package test.widget;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Frontlet;
import one.xis.FrontletParameter;
import one.xis.FrontletResponse;
import one.xis.HtmlFile;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.context.IntegrationTestContext;
import one.xis.validation.LabelKey;
import one.xis.validation.Mandatory;
import one.xis.validation.RegExpr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComplexFrontletFormFlowTest {

    private IntegrationTestContext context;
    private ProductService service;

    @BeforeEach
    void init() {
        service = new ProductService();
        context = IntegrationTestContext.builder()
                .withSingleton(service)
                .withSingleton(ProductShellPage.class)
                .withSingleton(ProductEditFrontlet.class)
                .withSingleton(ProductDetailsFrontlet.class)
                .build();
    }

    @Test
    void frontletFormKeepsValidationErrorsAndNavigatesToDetailsFrontletAfterCorrection() {
        var client = context.openPage("/frontlet-products.html");
        var document = client.getDocument();

        assertThat(document.getInputElementById("frontlet-id").getValue()).isEqualTo("77");
        assertThat(document.getInputElementById("frontlet-name").getValue()).isEqualTo("Tea");
        assertThat(document.getInputElementById("frontlet-stock").getValue()).isEqualTo("5");

        document.getInputElementById("frontlet-name").setValue("");
        document.getInputElementById("frontlet-stock").setValue("many");
        document.getElementById("frontlet-save").click();

        assertThat(service.saved).isNull();
        assertThat(document.getInputElementById("frontlet-name").getValue()).isEmpty();
        assertThat(document.getInputElementById("frontlet-stock").getValue()).isEqualTo("many");
        assertThat(document.getElementById("frontlet-name-message").getInnerText()).isNotBlank();
        assertThat(document.getElementById("frontlet-stock-message").getInnerText()).isNotBlank();

        document.getInputElementById("frontlet-name").setValue("Green Tea");
        document.getInputElementById("frontlet-stock").setValue("9");
        document.getElementById("frontlet-save").click();

        assertThat(service.saved).isEqualTo(new ProductForm(77, "Green Tea", "9"));
        assertThat(document.getElementById("frontlet-detail")).isNotNull();
        assertThat(document.getElementById("frontlet-detail").getInnerText()).isEqualTo("77:Green Tea:9");
    }

    record ProductForm(
            Integer id,

            @Mandatory
            @LabelKey("product.name")
            String name,

            @RegExpr("[0-9]+")
            @LabelKey("product.stock")
            String stock
    ) {
    }

    static class ProductService {
        ProductForm saved;

        ProductForm load() {
            return new ProductForm(77, "Tea", "5");
        }

        ProductForm get(Integer id) {
            return saved != null && saved.id().equals(id) ? saved : load();
        }

        void save(ProductForm form) {
            saved = form;
        }
    }

    @Page("/frontlet-products.html")
    @HtmlFile("/ComplexFrontletFormShellPage.html")
    static class ProductShellPage {
    }

    @Frontlet
    @HtmlFile("/ComplexFrontletFormFrontlet.html")
    static class ProductEditFrontlet {

        private final ProductService service;

        ProductEditFrontlet(ProductService service) {
            this.service = service;
        }

        @FormData("product")
        ProductForm product() {
            return service.load();
        }

        @Action
        FrontletResponse save(@FormData("product") ProductForm product) {
            service.save(product);
            return FrontletResponse.of(ProductDetailsFrontlet.class, "id", product.id());
        }
    }

    @Frontlet
    @HtmlFile("/ComplexFrontletFormDetailsFrontlet.html")
    static class ProductDetailsFrontlet {

        private final ProductService service;

        ProductDetailsFrontlet(ProductService service) {
            this.service = service;
        }

        @ModelData("detail")
        String detail(@FrontletParameter("id") Integer id) {
            var product = service.get(id);
            return product.id() + ":" + product.name() + ":" + product.stock();
        }
    }
}
