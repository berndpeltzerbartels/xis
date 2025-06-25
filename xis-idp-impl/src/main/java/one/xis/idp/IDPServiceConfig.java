package one.xis.idp;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISBean;
import one.xis.context.XISComponent;

import java.util.List;
import java.util.Optional;

@XISComponent
@RequiredArgsConstructor
class IDPServiceConfig {

    @XISBean
    Optional<LocalIDPService> idpService(List<LocalIDPUserService> idpUserServices) {
        return switch (idpUserServices.size()) {
            case 0 -> Optional.empty();
            case 1 -> Optional.of(new LocalIDPServiceImpl(idpUserServices.get(0)));
            default ->
                    throw new IllegalStateException("Multiple LocalUserInfoService instances found: " + idpUserServices.size());
        };
    }

}
