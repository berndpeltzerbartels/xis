package one.xis.test.dom;

import java.util.Arrays;

public class TestFile extends GraalVMProxy {

    private final String name;
    private final String type;
    private final byte[] bytes;
    private final int size;

    public TestFile(String name, String type, byte[] bytes) {
        this.name = name;
        this.type = type == null ? "application/octet-stream" : type;
        this.bytes = bytes == null ? new byte[0] : Arrays.copyOf(bytes, bytes.length);
        this.size = this.bytes.length;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public byte[] getBytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }
}
