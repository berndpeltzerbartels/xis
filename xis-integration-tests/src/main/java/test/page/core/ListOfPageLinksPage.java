package test.page.core;


import lombok.Setter;
import one.xis.ModelData;
import one.xis.Page;

@Page("/listOfLinks.html")
class ListOfPageLinksPage {

    @Setter
    private Link[] links;

    @ModelData("links")
    Link[] links() {
        return links;
    }

}
