package io.vproxy.windivert.pni;

import io.vproxy.pni.annotation.Bit;
import io.vproxy.pni.annotation.Struct;
import io.vproxy.pni.annotation.Unsigned;

/*
typedef struct
{
    INT64  Timestamp;
UINT32 Layer:8;
UINT32 Event:8;
UINT32 Sniffed:1;
UINT32 Outbound:1;
UINT32 Loopback:1;
UINT32 Impostor:1;
UINT32 IPv6:1;
UINT32 IPChecksum:1;
UINT32 TCPChecksum:1;
UINT32 UDPChecksum:1;
UINT32 Reserved1:8;
UINT32 Reserved2;
union
{
    WINDIVERT_DATA_NETWORK Network;
    WINDIVERT_DATA_FLOW Flow;
    WINDIVERT_DATA_SOCKET Socket;
    WINDIVERT_DATA_REFLECT Reflect;
    UINT8 Reserved3[64];
};
}
 */

@Struct
public class WinDivertAddress {
    @Unsigned long timestamp;
    @Bit({
        @Bit.Field(name = "layer", bits = 8),
        @Bit.Field(name = "event", bits = 8),
        @Bit.Field(name = "sniffed", bits = 1, bool = true),
        @Bit.Field(name = "outbound", bits = 1, bool = true),
        @Bit.Field(name = "loopback", bits = 1, bool = true),
        @Bit.Field(name = "impostor", bits = 1, bool = true),
        @Bit.Field(name = "ipv6", bits = 1, bool = true),
        @Bit.Field(name = "ipChecksum", bits = 1, bool = true),
        @Bit.Field(name = "tcpChecksum", bits = 1, bool = true),
        @Bit.Field(name = "udpChecksum", bits = 1, bool = true),
    })
    @Unsigned int bitfield0;
    @Unsigned int bitfield1;
    WinDivertData union0;
}
