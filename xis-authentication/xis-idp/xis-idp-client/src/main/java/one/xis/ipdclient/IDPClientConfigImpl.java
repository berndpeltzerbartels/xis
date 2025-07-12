package one.xis.ipdclient;

import lombok.Data;
import one.xis.server.UrlHolder;

@Data
public class IDPClientConfigImpl implements IDPClientConfig {

    private String idpId;
    private UrlHolder idpServerUrl;
    private String clientId;
    private String clientSecret;
}
