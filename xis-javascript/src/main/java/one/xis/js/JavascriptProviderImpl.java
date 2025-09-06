package one.xis.js;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.resource.Resource;
import one.xis.resource.StringResource;
import one.xis.utils.io.IOUtils;

import java.util.ArrayList;

@XISComponent
@RequiredArgsConstructor
class JavascriptProviderImpl implements JavascriptProvider {

    private final JavascriptExtensionLoader extensionLoader;
    private final JavascriptCompressor javascriptCompressor;

    @Getter
    private Resource compressedJavascript;

    @Getter
    private Resource sourceMap;

    @XISInit
    public void init() {
        var sources = new ArrayList<String>();
        sources.add(xisJs());
        sources.addAll(extensionLoader.loadExtensions());
        var result = javascriptCompressor.compress(sources);
        this.compressedJavascript = new StringResource(result.compressed());
        this.sourceMap = new StringResource(result.sourceMap());
    }


    private String xisJs() {
        return IOUtils.getClassPathResourceContent("xis.js");
    }
}
