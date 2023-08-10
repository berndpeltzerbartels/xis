package micronaut.example


class PageLink {
    def id
    def title
    def url

    PageLink(id, title, url) {
        this.id = id
        this.title = title
        this.url = url
    }
}