package one.xis.auth;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.xis.*;
import one.xis.auth.idp.ExternalIDPService;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static one.xis.auth.ExternalIdpSelectIdpUrlProvider.SELECT_IDP_URL;


@Setter
@Page(SELECT_IDP_URL)
@HtmlFile("/select-idp.html")
@DefaultHtmlFile("/default-select-idp.html")
@RequiredArgsConstructor
public class ExternalIdpSelectPageController {
    private Collection<ExternalIDPService> externalIDPServices;

    @ModelData("externalIdpIds")
    Collection<String> getExternalIdpIds() {
        return externalIDPServices.stream()
                .map(ExternalIDPService::getProviderId)
                .toList();
    }

    @ModelData("externalIdpUrls")
    Map<String, String> getExternalIdpUrls(@URLParameter("redirect_uri") String postLoginRedirectUrl) { // Annotation korrigiert
        return externalIDPServices.stream()
                .collect(Collectors.toMap(ExternalIDPService::getProviderId, service -> service.createLoginUrl(postLoginRedirectUrl)));
    }
}
