package test.page.binding.localstorage;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StorageBindingTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(StorageBindingPage.class)
                .withSingleton(ProductWidget.class)
                .build();
    }

    @Test
    void testInitialEmptyShoppingCard() {
        var result = testContext.openPage(StorageBindingPage.class);

        var spanItemCountCard = result.getDocument().getElementById("item-count-card");
        var spanItemCountProducts = result.getDocument().getElementById("item-count-products");
        var spanTotalPrice = result.getDocument().getElementById("total-price");

        assertThat(spanItemCountCard).isNotNull();
        assertThat(spanItemCountProducts).isNotNull();
        assertThat(spanTotalPrice).isNotNull();

        assertThat(spanItemCountCard.getInnerText()).isEqualTo("0 items");
        assertThat(spanItemCountProducts.getInnerText()).isEqualTo("0 items");
        //assertThat(spanTotalPrice.getInnerText()).isEqualTo("€0");
    }

    @Test
    void testAddSingleProduct() {
        var result = testContext.openPage(StorageBindingPage.class);

        var spanItemCountCard = result.getDocument().getElementById("item-count-card");
        var spanItemCountProducts = result.getDocument().getElementById("item-count-products");
        var spanTotalPrice = result.getDocument().getElementById("total-price");

        assertThat(spanItemCountCard).isNotNull();
        assertThat(spanItemCountProducts).isNotNull();
        assertThat(spanTotalPrice).isNotNull();

        assertThat(spanItemCountCard.getInnerText()).isEqualTo("0 items");
        assertThat(spanItemCountProducts.getInnerText()).isEqualTo("0 items");
        //assertThat(spanTotalPrice.getInnerText()).isEqualTo("€0");

        // Click first product link
        var link1 = result.getDocument().getElementById("add-product-1");
        link1.click();

        // Check updated cart
        assertThat(spanItemCountCard.getInnerText()).contains("1 items");
        assertThat(spanItemCountProducts.getInnerText()).contains("1 items");
        //  assertThat(spanTotalPrice.getInnerText()).contains("€10.99");
    }

    @Test
    void testAddMultipleProducts() {
        var result = testContext.openPage(StorageBindingPage.class);

        // Click first product link
        var link1 = result.getDocument().getElementById("add-product-1");
        var link2 = result.getDocument().getElementById("add-product-2");
        var link3 = result.getDocument().getElementById("add-product-3");

        link1.click();
        link2.click();
        link3.click();

        var spanItemCountCard = result.getDocument().getElementById("item-count-card");
        var spanItemCountProducts = result.getDocument().getElementById("item-count-products");
        var spanTotalPrice = result.getDocument().getElementById("total-price");


        assertThat(spanItemCountCard.getInnerText()).contains("3 items");
        assertThat(spanItemCountProducts.getInnerText()).contains("3 items");

        // Total: 10.99 + 25.50 + 15.75 = 52.24
        assertThat(spanTotalPrice.getInnerText()).contains("€52.24");
    }

    @Test
    void testAddSameProductMultipleTimes() {
        var result = testContext.openPage(StorageBindingPage.class);

        var spanItemCountCard = result.getDocument().getElementById("item-count-card");
        var spanItemCountProducts = result.getDocument().getElementById("item-count-products");
        var spanTotalPrice = result.getDocument().getElementById("total-price");

        assertThat(spanItemCountCard).isNotNull();
        assertThat(spanItemCountProducts).isNotNull();
        assertThat(spanTotalPrice).isNotNull();

        assertThat(spanItemCountCard.getInnerText()).isEqualTo("0 items");
        assertThat(spanItemCountProducts.getInnerText()).isEqualTo("0 items");
        assertThat(spanTotalPrice.getInnerText()).isEqualTo("€0");

        // Click first product link
        var link1 = result.getDocument().getElementById("add-product-1");
        link1.click();

        assertThat(spanItemCountCard.getInnerText()).contains("1 items");
        assertThat(spanItemCountProducts.getInnerText()).contains("1 items");
        assertThat(spanTotalPrice.getInnerText()).contains("€10.99");

        link1.click(); // Add same product again

        assertThat(spanItemCountCard.getInnerText()).contains("2 items");
        assertThat(spanItemCountProducts.getInnerText()).contains("2 items");
        assertThat(spanTotalPrice.getInnerText()).contains("€21.98");

        link1.click();

        assertThat(spanItemCountCard.getInnerText()).contains("3 items");
        assertThat(spanItemCountProducts.getInnerText()).contains("3 items");
        assertThat(spanTotalPrice.getInnerText()).contains("€32.97");
    }
}
