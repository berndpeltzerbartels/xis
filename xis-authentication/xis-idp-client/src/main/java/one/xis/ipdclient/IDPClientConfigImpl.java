package one.xis.ipdclient;

import lombok.Data;

@Data
public class IDPClientConfigImpl implements IDPClientConfig {

    private String idpId;
    private String idpServerUrl;
    private String clientId;
    private String clientSecret;
}
