package micronaut.example

class RepeatInsideRepeatPageItem {
    def title
    def subItems

    RepeatInsideRepeatPageItem(title, subItems) {
        this.title = title
        this.subItems = subItems
    }
}
