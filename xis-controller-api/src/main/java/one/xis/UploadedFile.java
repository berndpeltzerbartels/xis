package one.xis;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Uploaded file data made available to XIS controllers.
 */
public final class UploadedFile {

    private final String fieldName;
    private final String fileName;
    private final String contentType;
    private final byte[] bytes;

    public UploadedFile(String fieldName, String fileName, String contentType, byte[] bytes) {
        this.fieldName = fieldName;
        this.fileName = fileName;
        this.contentType = contentType;
        this.bytes = bytes == null ? new byte[0] : Arrays.copyOf(bytes, bytes.length);
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return bytes.length;
    }

    public byte[] getBytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    public String getUtf8Text() {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
