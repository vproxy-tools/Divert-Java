module io.vproxy.windivert {
    requires io.vproxy.base;
    requires io.vproxy.pni;

    exports io.vproxy.windivert;
    exports io.vproxy.windivert.pni;
    opens io.vproxy.windivert;
}
