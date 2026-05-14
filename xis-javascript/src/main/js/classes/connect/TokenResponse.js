class TokenResponse {
  constructor() {
    this.token = undefined;
    this.renewToken = undefined;
    this.tokenExpiresAt = -1;
    this.renewTokenExpiresAt = -1;
  }

  static fromJson(json) {
    const obj = JSON.parse(json);
    const response = new TokenReponse();
    response.token = obj.token;
    response.renewToken = obj.renewToken;
    response.tokenExpiresAt = obj.tokenExpiresAt;
    response.renewTokenExpiresAt = obj.renewTokenExpiresAt;
    return response;
  }
}