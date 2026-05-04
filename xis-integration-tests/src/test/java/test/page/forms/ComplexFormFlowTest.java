package test.page.forms;

import one.xis.Action;
import one.xis.FormData;
import one.xis.HtmlFile;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.PageResponse;
import one.xis.PathVariable;
import one.xis.QueryParameter;
import one.xis.context.IntegrationTestContext;
import one.xis.validation.LabelKey;
import one.xis.validation.Mandatory;
import one.xis.validation.RegExpr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComplexFormFlowTest {

    private IntegrationTestContext context;
    private ProductService service;

    @BeforeEach
    void init() {
        service = new ProductService();
        context = IntegrationTestContext.builder()
                .withSingleton(service)
                .withSingleton(ProductEditPage.class)
                .withSingleton(ProductDetailsPage.class)
                .build();
    }

    @Test
    void formKeepsSubmittedValuesOnValidationErrorAndPassesPathAndQueryParametersToAction() {
        var client = context.openPage("/shops/acme/products/42/edit.html?mode=quick");
        var document = client.getDocument();

        assertThat(document.getInputElementById("id").getValue()).isEqualTo("42");
        assertThat(document.getInputElementById("name").getValue()).isEqualTo("Coffee");
        assertThat(document.getInputElementById("stock").getValue()).isEqualTo("12");
        assertThat(document.getElementById("loaded-context").getInnerText()).isEqualTo("acme:42:quick");

        document.getInputElementById("name").setValue("");
        document.getInputElementById("stock").setValue("abc");
        document.getElementById("save").click();

        assertThat(service.saved).isNull();
        assertThat(document.getInputElementById("id").getValue()).isEqualTo("42");
        assertThat(document.getInputElementById("name").getValue()).isEmpty();
        assertThat(document.getInputElementById("stock").getValue()).isEqualTo("abc");
        assertThat(document.getElementById("name-message").getInnerText()).isEqualTo("Benutzerdefinierte Pflichtfeldmeldung");
        assertThat(document.getElementById("stock-message").getInnerText()).isEqualTo("Ungültige Eingabe");
        assertThat(document.getElementsByTagName("li").isEmpty()).isFalse();

        document.getInputElementById("name").setValue("Espresso");
        document.getInputElementById("stock").setValue("21");
        document.getElementById("save").click();

        assertThat(service.saved).isEqualTo(new ProductForm(42, "Espresso", "21"));
        assertThat(service.savedTenant).isEqualTo("acme");
        assertThat(service.savedId).isEqualTo(42);
        assertThat(service.savedMode).isEqualTo("quick");
        assertThat(document.getElementByTagName("title").getInnerText()).isEqualTo("Product details");
        assertThat(document.getElementById("detail-context").getInnerText()).isEqualTo("acme:42:quick:Espresso:21");
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
        String savedTenant;
        Integer savedId;
        String savedMode;

        ProductForm load(String tenant, Integer id, String mode) {
            return new ProductForm(id, "Coffee", "12");
        }

        ProductForm get(String tenant, Integer id, String mode) {
            return saved != null && saved.id().equals(id) ? saved : load(tenant, id, mode);
        }

        void save(String tenant, Integer id, String mode, ProductForm form) {
            savedTenant = tenant;
            savedId = id;
            savedMode = mode;
            saved = form;
        }
    }

    @Page("/shops/{tenant}/products/{id}/edit.html")
    @HtmlFile("/ComplexFormFlowPage.html")
    static class ProductEditPage {

        private final ProductService service;

        private String savedContext = "";

        ProductEditPage(ProductService service) {
            this.service = service;
        }

        @FormData("product")
        ProductForm product(@PathVariable("tenant") String tenant,
                            @PathVariable("id") Integer id,
                            @QueryParameter("mode") String mode) {
            return service.load(tenant, id, mode);
        }

        @ModelData("loadedContext")
        String loadedContext(@PathVariable("tenant") String tenant,
                             @PathVariable("id") Integer id,
                             @QueryParameter("mode") String mode) {
            return tenant + ":" + id + ":" + mode;
        }

        @ModelData("savedContext")
        String savedContext() {
            return savedContext;
        }

        @Action
        PageResponse save(@PathVariable("tenant") String tenant,
                          @PathVariable("id") Integer id,
                          @QueryParameter("mode") String mode,
                          @FormData("product") ProductForm product) {
            service.save(tenant, id, mode, product);
            savedContext = tenant + ":" + id + ":" + mode + ":" + product.name() + ":" + product.stock();
            return PageResponse.of(ProductDetailsPage.class, "tenant", tenant)
                    .pathVariable("id", id)
                    .queryParameter("mode", mode);
        }
    }

    @Page("/shops/{tenant}/products/{id}/details.html")
    @HtmlFile("/ComplexFormFlowDetailsPage.html")
    static class ProductDetailsPage {

        private final ProductService service;

        ProductDetailsPage(ProductService service) {
            this.service = service;
        }

        @ModelData("detailContext")
        String detailContext(@PathVariable("tenant") String tenant,
                             @PathVariable("id") Integer id,
                             @QueryParameter("mode") String mode) {
            var product = service.get(tenant, id, mode);
            return tenant + ":" + product.id() + ":" + mode + ":" + product.name() + ":" + product.stock();
        }
    }
}
