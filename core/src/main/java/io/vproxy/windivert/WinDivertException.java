package io.vproxy.windivert;

public class WinDivertException extends Exception {
    public WinDivertException() {
        super();
    }

    public WinDivertException(String message) {
        super(message);
    }

    public WinDivertException(Throwable cause) {
        super(cause);
    }

    public WinDivertException(String message, Throwable cause) {
        super(message, cause);
    }
}
