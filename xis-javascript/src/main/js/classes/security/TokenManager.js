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
    console.log('Initializing TokenManager');
    this.accessToken = this.readAccessTokenFromCookies();
    this.renewToken = this.readRenewTokenFromCookies();
    this.parseTokens();
  }

  parseTokens() {
    console.debug('Parsing tokens');
    this.tokenAttributes = new TokenAttributes();
    if (this.accessToken) {
      var accessTokenDecoded = this.decodeToken(this.accessToken);
      this.tokenAttributes.userId = accessTokenDecoded.sub || '';
      this.tokenAttributes.roles = accessTokenDecoded.roles || [];
      this.tokenAttributes.claims = accessTokenDecoded.claims || {};
      this.accessTokenExpiresAt = accessTokenDecoded.exp || -1;
    }
    if (this.renewToken) {
      var renewTokenDecoded = this.decodeToken(this.renewToken);
      this.tokenAttributes.userId = renewTokenDecoded.sub || this.tokenAttributes.userId;
      this.tokenAttributes.roles = renewTokenDecoded.roles || this.tokenAttributes.roles;
      this.tokenAttributes.claims = renewTokenDecoded.claims || this.tokenAttributes.claims;
      this.renewTokenExpiresAt = renewTokenDecoded.exp || -1;
    }
    console.log('Parsing tokens completed');
  }

  setTokens(response) {
    debugger;
    this.accessToken = response.accessToken;
    this.renewToken = response.renewToken; // Use existing renew token if not provided
    this.parseTokens();
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
  isAccessTokenExpiring() {
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
    debugger;
    if (!this.accessToken) {
      console.warn('No access token available');
      return null;
    }
    if (!this.isAccessTokenExpiring()) {
      console.debug('Access token is still valid');
      return this.accessToken;
    }
    console.warn('Access token is expiring, attempting to renew');

    if (this.isRenewTokenExpired()) {
      console.warn('Renew token is expired, cannot renew access token');
      return false; // Both tokens are expired
    }
    try {
      console.log('Renewing access token using renew token');
      const response = await app.client.renew(this.renewToken);
      this.setTokens(response);
      return this.accessToken;
    } catch (e) {
      return false;
    }
  }

  readAccessTokenFromCookies() {
    return this.readCookieValue('access_token');
  }

  readRenewTokenFromCookies() {
    return this.readCookieValue('refresh_token');
  }


  decodeToken(token) {
    try {
      return JSON.parse(this.base64UrlDecode(token.split('.')[1]));
    } catch (e) {
      console.error('Failed to decode token:', e);
      return null;
    }
  }

  base64UrlDecode(str) {
    // Ersetze URL-sichere Zeichen durch klassische Base64-Zeichen
    str = str.replace(/-/g, '+').replace(/_/g, '/');
    // Füge ggf. Padding hinzu
    while (str.length % 4 !== 0) {
      str += '=';
    }
    return atob(str);
  }


  readCookieValue(name) {
    const value = document.cookie
      .split('; ')
      .find(row => row.startsWith(name + '='));
    return value ? value.split('=')[1] : null;
  }
}
