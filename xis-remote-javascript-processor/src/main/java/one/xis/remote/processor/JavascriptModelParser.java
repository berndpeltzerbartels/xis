package one.xis.remote.processor;

import one.xis.remote.javascript.*;
import one.xis.template.TemplateModel;

import java.util.*;
import java.util.stream.Collectors;

public class JavascriptModelParser {

    JSAst parse(Collection<TemplateModel> templateModels, Collection<String> stateVars) {
        JSAst sourceCode = new JSAst();
        templateModels.forEach(model -> evaluate(model, sourceCode, stateVars));
        return sourceCode;
    }

    private void evaluate(TemplateModel templateModel, JSAst code,, Collection<String> stateVars) {
        code.add(createTemplateClass(templateModel, stateVars));
    }

    private JSClass createTemplateClass(TemplateModel templateModel, Collection<String> stateVars) {
        List<JSVar> parameters = Collections.singletonList(new JSVar("state"));
        JSFunction funct = new JSFunction("content", parameters, contentStatements(templateModel, stateVars));
        return new JSClass(templateModel.getName(), Set.of(funct));
    }

    private List<JSStatement> contentStatements(TemplateModel templateModel, Collection<String> stateVarNames) {
        List<JSStatement> statements = new ArrayList<>();

        List<JSVar> stateVars = stateVarNames.stream().map(JSVar::new).collect(Collectors.toList());


        stateVars.stream().map(JSVar::new).forEach(statements::add);
        stateVars.stream().map(stateVar -> new JSAssignment()).forEach(statements::add);
        return statements;
    }

    private void parse(model, JSAst code) {

    }
}
