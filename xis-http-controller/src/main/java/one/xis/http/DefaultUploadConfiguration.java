package one.xis.http;

import one.xis.UploadConfiguration;
import one.xis.context.ApplicationProperties;
import one.xis.context.DefaultComponent;

@DefaultComponent
public class DefaultUploadConfiguration implements UploadConfiguration {

    @Override
    public long getMaxFileSize() {
        return UploadConfiguration.parseSize(
                ApplicationProperties.getProperty("xis.upload.max-file-size"),
                UploadConfiguration.DEFAULT_MAX_FILE_SIZE
        );
    }

    @Override
    public long getMaxRequestSize() {
        return UploadConfiguration.parseSize(
                ApplicationProperties.getProperty("xis.upload.max-request-size"),
                UploadConfiguration.DEFAULT_MAX_REQUEST_SIZE
        );
    }
}
