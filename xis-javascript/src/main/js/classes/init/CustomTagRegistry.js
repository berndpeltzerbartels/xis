class CustomTagRegistry {

    constructor() {
        this.tagConfigs = [];
    }

    registerTag(tagConfig) {
        this.tagConfigs.push(tagConfig);
    }

    getAllTagConfigs() {
        return this.tagConfigs;
    }
}