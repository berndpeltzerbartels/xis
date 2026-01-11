function openPage(uri) {

}

function htmlToElement(html) {
    var holder = document.createElement('div');
    holder.innerHTML = html;
    for (var child of nodeListToArray(holder.childNodes)) {
        if (isElement(child)) {
            return child;
        }
    }
}


function readHeadChildArray(content) {
    const parser = new DOMParser();
    const doc = parser.parseFromString(content, 'text/html');
    const head = doc.head;
    return head ? Array.from(head.children) : [];
}


function readBodyChildArray(html) {
    const parser = new DOMParser();
    const doc = parser.parseFromString(html, 'text/html');
    const body = doc.body;
    return body ? Array.from(body.children) : [];
}


/**
 * Check if WebSocket connection is available
 * @returns {Promise<boolean>}
 */
function checkWebSocketAvailable() {
    return new Promise((resolve) => {
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const host = window.location.host; // includes port
        const url = protocol + '//' + host + '/ws';
        
        console.log('Testing WebSocket connection to: ' + url);
        
        let ws = null;
        let resolved = false;
        
        const cleanup = () => {
            if (ws) {
                ws.onopen = null;
                ws.onerror = null;
                ws.onclose = null;
                try {
                    ws.close();
                } catch (e) {
                    // ignore
                }
                ws = null;
            }
        };
        
        const finish = (result) => {
            if (!resolved) {
                resolved = true;
                cleanup();
                console.log('WebSocket available: ' + result);
                resolve(result);
            }
        };
        
        // Timeout
        setTimeout(() => finish(false), 2000); // 2 seconds timeout
        
        try {
            ws = new WebSocket(url);
            
            ws.onopen = () => {
                finish(true);
            };
            
            ws.onerror = () => {
                finish(false);
            };
            
            ws.onclose = () => {
                finish(false);
            };
            
        } catch (e) {
            console.error('WebSocket test failed:', e);
            finish(false);
        }
    });
}



