package one.xis.security;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISBean;
import one.xis.context.XISComponent;

import java.util.List;
import java.util.Optional;

@XISComponent
@RequiredArgsConstructor
class IDPServiceConfig {

    @XISBean
    Optional<IDPService> idpService(List<IDPUserService> idpUserServices) {
        return switch (idpUserServices.size()) {
            case 0 -> Optional.empty();
            case 1 -> Optional.of(new IDPServiceImpl(idpUserServices.get(0)));
            default ->
                    throw new IllegalStateException("Multiple LocalUserInfoService instances found: " + idpUserServices.size());
        };
    }

}
