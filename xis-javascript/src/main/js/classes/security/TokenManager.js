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
    this.tokenAttributes = new TokenAttributes();
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
      this.renewTokenExpiresAt = renewTokenDecoded.exp || -1;
    }
    console.log('Parsing tokens completed');
  }

  setAccessToken(token) {
    console.log('Setting access token:', token);
    this.accessToken = token;
    const decodedToken = this.decodeToken(token);
    this.accessTokenExpiresAt = decodedToken.exp || -1;
    this.tokenAttributes.userId = decodedToken.sub || '';
    this.tokenAttributes.roles = decodedToken.roles || [];
    this.tokenAttributes.claims = decodedToken.claims || {};
  }

  setRenewToken(token) {
    console.log('Setting renew token:', token);
    this.renewToken = token;
    const decodedToken = this.decodeToken(token);
    this.renewTokenExpiresAt = decodedToken.exp || -1;
  }

  setTokens(response) {
    this.accessToken = response.accessToken;
    this.renewToken = response.renewToken;
    this.parseTokens();
  }

  reset() {
    console.log('Resetting TokenManager');
    this.accessToken = null;
    this.accessTokenExpiresAt = -1;
    this.renewToken = null;
    this.renewTokenExpiresAt = -1;
    this.tokenAttributes = new TokenAttributes();
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
      console.debug('No access token available');
      return null;
    }
    if (!this.isAccessTokenExpiring()) {
      console.debug('Access token is still valid');
      return this.accessToken;
    }
    console.log('Access token is expiring, attempting to renew');
    try {
      console.debug('Renewing access token using renew token');
      const response = await app.client.renew(this.renewToken);
      this.setTokens(response);
      return this.accessToken;
    } catch (e) {
      return false;
    }
  }

  isAccessTokenExpiring() {
    if (!this.accessToken || this.accessTokenExpiresAt <= 0) {
      return false; // No access token or invalid expiration
    }
    const now = Math.floor(Date.now() / 1000); // Current time in seconds
    return this.accessTokenExpiresAt - now < 60; // Less than 60 seconds to expiration
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
