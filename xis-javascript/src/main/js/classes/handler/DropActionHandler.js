class DropActionHandler extends TagHandler {

    constructor(element, client, frontletContainers) {
        super(element);
        this.type = 'drop-action-handler';
        this.client = client;
        this.frontletContainers = frontletContainers;
        this.targetContainerId = element.getAttribute('xis:target-container');
        element.addEventListener('dragover', event => event.preventDefault());
        element.addEventListener('drop', event => this.onDrop(event));
    }

    refresh(data) {
        this.data = data;
        this.targetContainerId = this.tag.getAttribute('xis:target-container');
        return this.refreshDescendantHandlers(data);
    }

    onDrop(event) {
        event.preventDefault();
        const dragPayload = this.readDragPayload(event);
        if (!dragPayload) {
            return;
        }
        const invocation = this.createActionInvocation(dragPayload);
        const frontletContainerHandler = this.findParentFrontletContainerHandler();
        const targetContainerHandler = this.targetContainerId ? app.tagHandlers.getHandler(this.frontletContainers.findContainer(this.targetContainerId)) : null;
        if (frontletContainerHandler) {
            return this.frontletAction(frontletContainerHandler, targetContainerHandler, invocation);
        } else {
            return this.pageAction(targetContainerHandler, invocation);
        }
    }

    readDragPayload(event) {
        const raw = this.dataTransfer(event).getData(DragHandler.mimeType());
        if (!raw) {
            return undefined;
        }
        try {
            return JSON.parse(raw);
        } catch (e) {
            return undefined;
        }
    }

    dataTransfer(event) {
        return event.dataTransfer ? event.dataTransfer : event.getDataTransfer();
    }

    createActionInvocation(dragPayload) {
        const expression = this.getAttribute('xis:drop');
        const open = expression.indexOf('(');
        const close = expression.lastIndexOf(')');
        if (open < 1 || close < open) {
            throw new Error('xis:drop must use actionName(arg1, arg2) syntax.');
        }
        const action = expression.substring(0, open).trim();
        const parameterSources = this.splitArguments(expression.substring(open + 1, close));
        const dropData = new Data({}, this.data);
        if (!this.isSimpleIdentifier(dragPayload.name)) {
            throw new Error('xis:drag variable name must be a simple identifier.');
        }
        dropData.setValue([dragPayload.name], dragPayload.value);

        const actionParameters = {};
        for (let i = 0; i < parameterSources.length; i++) {
            const source = parameterSources[i].trim();
            const namedArgument = this.parseNamedArgument(source);
            const expressionSource = namedArgument ? namedArgument.expression : source;
            const value = new ExpressionParser(elFunctions).parse(expressionSource).evaluate(dropData);
            actionParameters['$' + i] = value;
            if (namedArgument) {
                actionParameters[namedArgument.name] = value;
            } else if (this.isSimpleIdentifier(source)) {
                actionParameters[source] = value;
            }
        }
        return { action: action, actionParameters: actionParameters };
    }

    parseNamedArgument(source) {
        const separator = this.findTopLevelAssignment(source);
        if (separator < 1) {
            return undefined;
        }
        const name = source.substring(0, separator).trim();
        if (!this.isSimpleIdentifier(name)) {
            return undefined;
        }
        return {
            name: name,
            expression: source.substring(separator + 1).trim()
        };
    }

    findTopLevelAssignment(source) {
        let quote = undefined;
        let depth = 0;
        for (let i = 0; i < source.length; i++) {
            const c = source.charAt(i);
            if (quote) {
                if (c === quote && source.charAt(i - 1) !== '\\') {
                    quote = undefined;
                }
            } else if (c === '\'' || c === '"') {
                quote = c;
            } else if (c === '(' || c === '[') {
                depth++;
            } else if (c === ')' || c === ']') {
                depth--;
            } else if (c === '=' && depth === 0
                    && source.charAt(i - 1) !== '='
                    && source.charAt(i - 1) !== '!'
                    && source.charAt(i - 1) !== '<'
                    && source.charAt(i - 1) !== '>'
                    && source.charAt(i + 1) !== '=') {
                return i;
            }
        }
        return -1;
    }

    splitArguments(source) {
        const result = [];
        let buffer = '';
        let quote = undefined;
        let depth = 0;
        for (let i = 0; i < source.length; i++) {
            const c = source.charAt(i);
            if (quote) {
                buffer += c;
                if (c === quote && source.charAt(i - 1) !== '\\') {
                    quote = undefined;
                }
            } else if (c === '\'' || c === '"') {
                quote = c;
                buffer += c;
            } else if (c === '(' || c === '[') {
                depth++;
                buffer += c;
            } else if (c === ')' || c === ']') {
                depth--;
                buffer += c;
            } else if (c === ',' && depth === 0) {
                result.push(buffer);
                buffer = '';
            } else {
                buffer += c;
            }
        }
        if (buffer.trim().length > 0) {
            result.push(buffer);
        }
        return result;
    }

    isSimpleIdentifier(source) {
        return /^[A-Za-z_$][A-Za-z0-9_$]*$/.test(source);
    }

    frontletAction(frontletContainerHandler, targetContainerHandler, invocation) {
        if (!targetContainerHandler) {
            targetContainerHandler = frontletContainerHandler;
        }
        return this.client.frontletLinkAction(frontletContainerHandler.frontletInstance, frontletContainerHandler.frontletState, invocation.action, invocation.actionParameters)
            .then(response => this.handleActionResponse(response, targetContainerHandler));
    }

    pageAction(targetContainerHandler, invocation) {
        return this.client.pageLinkAction(app.pageController.resolvedURL, invocation.action, invocation.actionParameters)
            .then(response => this.handleActionResponse(response, targetContainerHandler));
    }

    handleActionResponse(response, targetContainerHandler) {
        switch (response.actionProcessing) {
            case 'NONE':
                return Promise.resolve();
            case 'PAGE':
                return app.pageController.handleActionResponse(response);
            case 'FRONTLET':
                if (targetContainerHandler) {
                    return targetContainerHandler.handleActionResponse(response);
                }
                return Promise.resolve();
            case 'MODAL':
                return app.modals.handleActionResponse(response, targetContainerHandler);
            default:
                throw new Error('Unknown action processing type: ' + response.actionProcessing);
        }
    }
}
