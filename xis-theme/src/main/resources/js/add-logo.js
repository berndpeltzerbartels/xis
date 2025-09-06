
function addNavLogo() {
   var nav = findNavElement();
   if (nav && !containsLogo(nav)) {
       addLogoDiv(nav);
   }
}

function findNavElement() {
    var list =  document.getElementsByTagName('nav');
    if (list.length > 0) {
        return list.item(0);
    }
    return null;
}

function containsLogo(nav) {
    var children = nav.children;
    for (var i = 0; i < children.length; i++) {
        var child = children.item(i);
        if (child.classList.contains('logo')) {
            return true;
        }
    }
    return false;
}


function addLogoDiv(nav) {
    var div = document.createElement('div');
    div.classList.add('logo');
    var logoImg = document.createElement('img');
    logoImg.src = 'Xis1.svg'; // Path to your logo image
    logoImg.alt = 'Logo';
    logoImg.style.height = '40px'; // Adjust the height as needed
    logoImg.style.marginRight = '10px'; // Optional: Add some margin to the right
    // Insert the logo at the beginning of the nav element
    div.appendChild(logoImg);
    nav.insertBefore(div, nav.firstChild);
}

// Wait for the DOM to be fully loaded
eventListenerRegistry.addEventListener(EventType.PAGE_LOADED, function() {
    addNavLogo();
});

// Add the logo to the navigation bar
