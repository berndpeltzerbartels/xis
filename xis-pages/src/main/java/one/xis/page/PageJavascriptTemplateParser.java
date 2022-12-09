package one.xis.page;

import one.xis.context.XISComponent;
import one.xis.js.*;
import one.xis.template.PageTemplateModel;

import static one.xis.js.JavascriptAbstractClasses.XIS_PAGE;

@XISComponent
class PageJavascriptTemplateParser extends JavascriptTemplateParser<PageTemplateModel> {

    @Override
    public JSClass parseTemplateModel(PageTemplateModel pageTemplateModel, String javascriptClassName, JSScript script) {
        var pageClass = derrivedClass(javascriptClassName, XIS_PAGE, script);
        var headClass = toClass(pageTemplateModel.getHead(), script);
        var bodyClass = toClass(pageTemplateModel.getBody(), script);
        pageClass.addField("head", new JSContructorCall(headClass, "this"));
        pageClass.addField("body", new JSContructorCall(bodyClass, "this"));
        pageClass.addField("id", new JSString(pageTemplateModel.getKey()));
        pageClass.addField("server", new JSString("")); // empty = this server TODO method parameter
        return pageClass;
    }
}
