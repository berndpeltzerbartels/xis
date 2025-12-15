package test.xis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import one.xis.*;
import one.xis.context.IntegrationTestContext;
import one.xis.context.XISInit;
import one.xis.gson.JsonMap;
import one.xis.http.RequestContext;
import one.xis.server.ClientRequest;
import one.xis.server.FrontendServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class FormatterRecordIntegrationTest {
    private FrontendServiceImpl frontendService;
    private IntegrationTestContext context;
    private final ObjectMapper objectMapper = createObjectMapper();

    @BeforeEach
    void initFrontendService() {
        context = IntegrationTestContext.builder()
                .withSingleton(ProductRecordController.class)
                .build();
        frontendService = context.getSingleton(FrontendServiceImpl.class);
        RequestContext.createInstance(null, null);
    }

    @Test
    void formatterOnRecordComponent() throws JsonProcessingException {
        // given - als JSON kommt ein String im deutschen Datumsformat
        var json = "{\"name\":\"Laptop\",\"availableUntil\":\"31.12.2025\"}";
        var request = new ClientRequest();
        request.setPageId("/product.html");
        request.setPageUrl("/product.html");
        request.setAction("saveProduct");
        request.setFormData(JsonMap.of("product", json));
        request.setLocale(Locale.GERMANY);
        request.setZoneId("Europe/Berlin");

        // when
        var response = frontendService.processActionRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        var dataTree = objectMapper.valueToTree(response.getFormData());
        var savedProduct = objectMapper.treeToValue(dataTree.at("/product"), ProductRecord.class);
        
        assertThat(savedProduct.name()).isEqualTo("Laptop");
        assertThat(savedProduct.availableUntil()).isEqualTo(LocalDate.of(2025, 12, 31));
    }

    private ObjectMapper createObjectMapper() {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    record ProductRecord(
            String name,
            @UseFormatter(GermanDateFormatter.class)
            LocalDate availableUntil
    ) {
    }

    static class GermanDateFormatter implements Formatter<LocalDate> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        @Override
        public LocalDate parse(String value, Locale locale, ZoneId zoneId) {
            return LocalDate.parse(value, FORMATTER);
        }

        @Override
        public String format(LocalDate value, Locale locale, ZoneId zoneId) {
            return value.format(FORMATTER);
        }
    }

    @HtmlFile("/ProductRecordController.html")
    @Page("/product.html")
    static class ProductRecordController {

        @Getter
        private ProductRecord productData;

        @XISInit
        void init() {
            productData = new ProductRecord("Default Product", LocalDate.of(2025, 12, 31));
        }

        @ModelData("product")
        ProductRecord productData() {
            return productData;
        }

        @Action("saveProduct")
        void saveProduct(@FormData("product") ProductRecord product) {
            this.productData = product;
        }
    }
}
