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

  /**
   * @public
   * Sets the token and expiration values.
   * Should be called after login or token renewal.
   * 
   * @param {{token: string, exp: number, renewToken: string, renewExp: number}} tokens
   */
  setTokens(tokens) {
    this.accessToken = tokens.token;
    this.accessTokenExpiresAt = tokens.accessTokenExpiresAt;
    this.renewToken = tokens.renewToken;
    this.renewTokenExpiresAt = tokens.renewTokenExpiresAtwExp;
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
      return false;
    }

    try {
      const response = await app.client.renew(this.renewToken);
      this.setTokens(response);
      return this.token;
    } catch (e) {
      return false;
    }
  }
}
