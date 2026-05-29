package one.xis.context.springlike;

@SpringLikeConfiguration
public class SpringLikeProductConfig {

    @SpringLikeBean
    SpringLikeProductCatalog productCatalog() {
        return new SpringLikeProductCatalog("test catalog");
    }
}
