package io.vproxy.windivert.pni;

import io.vproxy.pni.annotation.Len;
import io.vproxy.pni.annotation.Struct;
import io.vproxy.pni.annotation.Unsigned;

@Struct
public class WinDivertDataSocket {
    @Unsigned long endpointId;
    @Unsigned long parentEndpointId;
    @Unsigned int processId;
    @Len(4) @Unsigned int[] localAddr;
    @Len(4) @Unsigned int[] remoteAddr;
    @Unsigned short localPort;
    @Unsigned short remotePort;
    @Unsigned byte protocol;
}
