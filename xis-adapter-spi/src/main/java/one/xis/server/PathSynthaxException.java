package one.xis.server;

class PathSynthaxException extends RuntimeException {
    PathSynthaxException(char ch, String message) {
        super("Malformed path at character '" + ch + "' :" + message);
    }
}
