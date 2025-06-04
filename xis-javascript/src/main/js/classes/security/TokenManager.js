/**
 * TokenManager handles access and refresh tokens,
 * including automatic renewal when the access token expires.
 */
class TokenManager {
  /**
   * @param {Client} client
   */
  constructor(client) {
    /** @private */
    this.client = client;

    /** @private */
    this.accessToken = null;

    /** @private */
    this.accessTokenExpiresAt = 0;

    /** @private */
    this.renewToken = null;

    /** @private */
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
    return !this.accessToken || Date.now() > this.accessTokenExpiresAt;
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
  async actualToken() {
    if (!this.isAccessTokenExpired()) {
      return this.token;
    }

    if (this.isRenewTokenExpired()) {
      return false;
    }

    try {
      const response = await this.client.renew(this.renewToken);
      this.setTokens(response);
      return this.token;
    } catch (e) {
      return false;
    }
  }
}
