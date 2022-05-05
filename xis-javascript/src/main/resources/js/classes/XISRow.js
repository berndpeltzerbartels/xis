/**
 * Encapsulates a list of elements for being repeated within
 * a XISLoop or XISIf.
 */
class XISRow {

  /**
   * 
   * @param {*} parent 
   * @param {*} childFactory
   */
    constructor(parent, childFactory) {
        this.parent = parent;
        this.childFactory = childFactory;
    }

    init() {
        this.childFactory().forEach(child => {
            
        });
    }


}