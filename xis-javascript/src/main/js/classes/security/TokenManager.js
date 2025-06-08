/**
 * TokenManager handles access and refresh tokens,
 * including automatic renewal when the access token expires.
 */
class TokenManager {
  /**
   * @constructor
   */
  constructor() {
    this.accessToken = null;
    this.accessTokenExpiresAt = 0;
    this.renewToken = null;
    this.renewTokenExpiresAt = 0;
  }


  init() {
    this.accessToken = this.readAccessTokenFromCookies();
    this.renewToken = this.readRenewTokenFromCookies();
    this.tokenAttributes = new TokenAttributes();
    if (this.accessToken) {
      var accessTokenDecoded = this.parseJwt(this.accessToken);
      this.tokenAttributes.userId = accessTokenDecoded.sub || '';
      this.tokenAttributes.roles = accessTokenDecoded.roles || [];
      this.tokenAttributes.claims = accessTokenDecoded.claims || {};
      this.accessTokenExpiresAt = accessTokenDecoded.exp || -1;
    }
    if (this.renewToken) {
      var renewTokenDecoded = this.parseJwt(this.renewToken);
      this.tokenAttributes.userId = renewTokenDecoded.sub || this.tokenAttributes.userId;
      this.tokenAttributes.roles = renewTokenDecoded.roles || this.tokenAttributes.roles;
      this.tokenAttributes.claims = renewTokenDecoded.claims || this.tokenAttributes.claims;
      this.renewTokenExpiresAt = renewTokenDecoded.exp || -1;
    }
  }

  reset() {
    this.accessToken = null;
    this.accessTokenExpiresAt = -1;
    this.renewToken = null;
    this.renewTokenExpiresAt = -1;
    this.tokenAttributes = new TokenAttributes();
  }


  /**
   * @private
   * Checks if the access token has expired.
   * 
   * @returns {boolean}
   */
  isAccessTokenExpired() {
    return !this.accessToken || Date.now() > (this.accessTokenExpiresAt - 5000); // 5 seconds buffer
  }

  /**
   * @private
   * Checks if the refresh token has expired.
   * 
   * @returns {boolean}
   */
  isRenewTokenExpired() {
    return !this.renewToken || Date.now() > this.renewTokenExpiresAt;
  }

  /**
   * @public
   * Returns the current valid access token.
   * If the token is expired but the refresh token is still valid,
   * it attempts to renew and returns the new access token.
   * Returns `false` if both tokens are expired.
   * 
   * @returns {Promise<string|false>}
   */
  async actualAccessToken() {
    if (!this.accessToken) {
      return null;
    }
    if (!this.isAccessTokenExpired()) {
      return this.token;
    }
    if (this.isRenewTokenExpired()) {
      return false; // Both tokens are expired
    }
    try {
      const response = await app.client.renew(this.renewToken);
      this.setTokens(response);
      return this.token;
    } catch (e) {
      return false;
    }
  }

  readAccessTokenFromCookies() {
    return this.readCookieValue('access_token');
  }

  readRenewTokenFromCookies() {
    return this.readCookieValue('renew_token');
  }


  parseJwt(token) {
    try {
      const payload = token.split('.')[1];
      return JSON.parse(atob(payload));
    } catch (e) {
      return null;
    }
  }

  readCookieValue(name) {
    const value = document.cookie
      .split('; ')
      .find(row => row.startsWith(name + '='));
    return value ? value.split('=')[1] : null;
  }
}
