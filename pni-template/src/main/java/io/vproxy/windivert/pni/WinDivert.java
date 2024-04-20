package io.vproxy.windivert.pni;

import io.vproxy.pni.annotation.*;
import io.vproxy.pni.array.IntArray;

import java.lang.foreign.MemorySegment;

@Downcall
public interface WinDivert {
    @Style(Styles.critical)
    @Name("WinDivertOpen")
    MemorySegment open(String filter, int layer, short priority, @Unsigned long flags);

    @Style(Styles.critical)
    @Name("WinDivertRecv")
    boolean recv(MemorySegment handle, MemorySegment pPacket, @Unsigned int packetLen, @NativeType("uint32_t*") @Raw int[] pRecvLen, WinDivertAddress pAddr);

    @Style(Styles.critical)
    @Name("WinDivertSend")
    boolean send(MemorySegment handle, MemorySegment pPacket, @Unsigned int packetLen, @NativeType("uint32_t*") @Raw int[] pSendLen, WinDivertAddress pAddr);

    @Style(Styles.critical)
    @Name("WinDivertClose")
    boolean close(MemorySegment handle);
}
