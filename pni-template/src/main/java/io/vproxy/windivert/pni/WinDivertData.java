package io.vproxy.windivert.pni;

import io.vproxy.pni.annotation.Len;
import io.vproxy.pni.annotation.Union;
import io.vproxy.pni.annotation.Unsigned;

@Union
public class WinDivertData {
    WinDivertDataNetwork network;
    WinDivertDataFlow flow;
    WinDivertDataSocket socket;
    WinDivertDataReflect reflect;
    @Len(64) @Unsigned byte[] reserved0;
}
