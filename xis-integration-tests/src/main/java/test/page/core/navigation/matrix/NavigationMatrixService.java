package test.page.core.navigation.matrix;

class NavigationMatrixService {

    private int pageRefreshCount;
    private int frontletRefreshCount;

    int nextPageRefreshCount() {
        return ++pageRefreshCount;
    }

    int nextFrontletRefreshCount() {
        return ++frontletRefreshCount;
    }
}
