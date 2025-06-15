/**
 * @typedef {Object} Tokens
 * @property {string} token - The access token.
 * @property {number} accessTokenExpiresAt - The expiration time of the access token in milliseconds since epoch.
 * @property {string} renewToken - The refresh token.
 * @property {number} renewTokenExpiresAt - The expiration time of the refresh token in milliseconds since epoch.
 */
class Tokens {
    constructor() {
        this.token = '';
        this.accessTokenExpiresAt = -1;
        this.renewToken = '';
        this.renewTokenExpiresAt = -1;
    }
}