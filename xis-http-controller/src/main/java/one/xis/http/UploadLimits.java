package one.xis.http;

import one.xis.UploadConfiguration;
import one.xis.UploadedFile;

import java.util.List;
import java.util.Map;

final class UploadLimits {

    private UploadLimits() {
    }

    static void validateRequestSize(HttpRequest request, UploadConfiguration uploadConfiguration) {
        long maxRequestSize = uploadConfiguration.getMaxRequestSize();
        int contentLength = request.getContentLength();
        if (contentLength > maxRequestSize) {
            throw requestLimitExceeded(maxRequestSize);
        }

        long uploadedBytes = uploadedBytes(request.getUploadedFiles());
        if (uploadedBytes > maxRequestSize) {
            throw requestLimitExceeded(maxRequestSize);
        }
    }

    private static long uploadedBytes(Map<String, List<UploadedFile>> uploadedFiles) {
        long total = 0;
        for (List<UploadedFile> files : uploadedFiles.values()) {
            for (UploadedFile file : files) {
                total += file.getSize();
                if (total < 0) {
                    return Long.MAX_VALUE;
                }
            }
        }
        return total;
    }

    private static UploadLimitExceededException requestLimitExceeded(long maxRequestSize) {
        return new UploadLimitExceededException("Multipart request exceeds the maximum size of " + maxRequestSize + " bytes");
    }
}
