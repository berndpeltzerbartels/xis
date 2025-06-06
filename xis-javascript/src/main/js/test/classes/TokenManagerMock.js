class TokenManagerMock {
    

    setTokens(response) {
        // NOOP: Mock implementation does not set tokens
    }

    actualAccessToken() {
        return "mockAccessToken";
    }

    getAccessTokenExpiresAt() {
        return 9999999999999;
    }

    getRenewToken() {
        return "mockRenewToken";
    }
}