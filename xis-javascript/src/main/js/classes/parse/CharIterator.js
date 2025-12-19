/**
 * Util-class to navigate among
 * a string's characters.
 */
class CharIterator {

    /**
     *
     * @param {string} src
     */
    constructor(src) {
        if (!isSet(src)) {
            src = '';
        }
        this.src = src;
        this.index = -1;
    }

    /**
     * @public
     * @returns {boolean}
     */
    hasNext() {
        return this.index + 1 < this.src.length;
    }

    /**
     * @public
     * @returns {any}
     */
    current() {
        return this.src[this.index];
    }

    /**
     * @public
     * @returns {any}
     */
    next() {
        this.index++;
        return this.src[this.index];
    }

    /**
     * @public
     * @returns {any}
     */
    beforeCurrent() {
        return this.index - 1 > -1 ? this.src[this.index - 1] : undefined;
    }

    /**
     * @public
     * @returns {any}
     */
    afterCurrent() {
        return this.index + 1 < this.src.length ? this.src[this.index + 1] : undefined;
    }

}