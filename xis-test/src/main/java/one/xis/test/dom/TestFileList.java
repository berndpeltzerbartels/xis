package one.xis.test.dom;

import org.graalvm.polyglot.proxy.ProxyArray;

import java.util.ArrayList;
import java.util.List;

public class TestFileList extends GraalVMProxy implements ProxyArray {

    private final List<TestFile> files = new ArrayList<>();
    public int length;

    public void setFile(TestFile file) {
        files.clear();
        if (file != null) {
            files.add(file);
        }
        length = files.size();
    }

    public TestFile item(int index) {
        return index >= 0 && index < files.size() ? files.get(index) : null;
    }

    public int getLength() {
        return length;
    }

    @Override
    public Object get(long index) {
        return item((int) index);
    }

    @Override
    public void set(long index, org.graalvm.polyglot.Value value) {
        throw new UnsupportedOperationException("FileList is read-only");
    }

    @Override
    public boolean remove(long index) {
        throw new UnsupportedOperationException("FileList is read-only");
    }

    @Override
    public long getSize() {
        return files.size();
    }
}
