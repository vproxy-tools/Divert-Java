package io.vproxy.windivert;

import io.vproxy.vpacket.AbstractIpPacket;

import java.lang.foreign.MemorySegment;

public record WinDivertPacketRecv(AbstractIpPacket packet, MemorySegment raw) {
}
