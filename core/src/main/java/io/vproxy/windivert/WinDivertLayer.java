package io.vproxy.windivert;

public enum WinDivertLayer {
    WINDIVERT_LAYER_NETWORK(0),
    WINDIVERT_LAYER_NETWORK_FORWARD(1),
    WINDIVERT_LAYER_FLOW(2),
    WINDIVERT_LAYER_SOCKET(3),
    WINDIVERT_LAYER_REFLECT(4),
    ;
    public final int value;

    WinDivertLayer(int value) {
        this.value = value;
    }
}
