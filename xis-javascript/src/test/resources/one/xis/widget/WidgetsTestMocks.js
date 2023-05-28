var client = {
    loadWidget: function(id) {
         return new Promise((resolve, reject) => {
            resolve(`<xis:template>
                        <h5>Widget</h5>
                        <div>

                        </div>
                    </xis:template>
            `)
         });
    }
}

var config = {
    widgetIds: ['widgetId'],
    widgetAttributes: {
        'widgetId': {}
    }
}


 function getChildElementByName(parent, childName) {
     for (var i = 0; i < parent.childNodes.length; i++) {
         var child = parent.childNodes.item(i);
         if (isElement(child) && child.localName == childName) {
             return child;
         }
     }
 }

 function isElement(node) {
     return node.nodeType == 1;
 }



 function initializeElement(element) {
 }