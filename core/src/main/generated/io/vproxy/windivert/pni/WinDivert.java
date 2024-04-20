package io.vproxy.windivert.pni;

import io.vproxy.pni.*;
import io.vproxy.pni.array.*;
import java.lang.foreign.*;
import java.lang.invoke.*;
import java.nio.ByteBuffer;

public class WinDivert {
    private WinDivert() {
    }

    private static final WinDivert INSTANCE = new WinDivert();

    public static WinDivert get() {
        return INSTANCE;
    }

    private static final MethodHandle openMH = PanamaUtils.lookupPNICriticalFunction(new PNILinkOptions(), MemorySegment.class, "WinDivertOpen", String.class /* filter */, int.class /* layer */, short.class /* priority */, long.class /* flags */);

    public MemorySegment open(PNIString filter, int layer, short priority, long flags) {
        MemorySegment RESULT;
        try {
            RESULT = (MemorySegment) openMH.invokeExact((MemorySegment) (filter == null ? MemorySegment.NULL : filter.MEMORY), layer, priority, flags);
        } catch (Throwable THROWABLE) {
            throw PanamaUtils.convertInvokeExactException(THROWABLE);
        }
        if (RESULT.address() == 0) return null;
        return RESULT;
    }

    private static final MethodHandle recvMH = PanamaUtils.lookupPNICriticalFunction(new PNILinkOptions(), boolean.class, "WinDivertRecv", MemorySegment.class /* handle */, MemorySegment.class /* pPacket */, int.class /* packetLen */, MemorySegment.class /* pRecvLen */, io.vproxy.windivert.pni.WinDivertAddress.LAYOUT.getClass() /* pAddr */);

    public boolean recv(MemorySegment handle, MemorySegment pPacket, int packetLen, IntArray pRecvLen, io.vproxy.windivert.pni.WinDivertAddress pAddr) {
        boolean RESULT;
        try {
            RESULT = (boolean) recvMH.invokeExact((MemorySegment) (handle == null ? MemorySegment.NULL : handle), (MemorySegment) (pPacket == null ? MemorySegment.NULL : pPacket), packetLen, (MemorySegment) (pRecvLen == null ? MemorySegment.NULL : pRecvLen.MEMORY), (MemorySegment) (pAddr == null ? MemorySegment.NULL : pAddr.MEMORY));
        } catch (Throwable THROWABLE) {
            throw PanamaUtils.convertInvokeExactException(THROWABLE);
        }
        return RESULT;
    }

    private static final MethodHandle sendMH = PanamaUtils.lookupPNICriticalFunction(new PNILinkOptions(), boolean.class, "WinDivertSend", MemorySegment.class /* handle */, MemorySegment.class /* pPacket */, int.class /* packetLen */, MemorySegment.class /* pSendLen */, io.vproxy.windivert.pni.WinDivertAddress.LAYOUT.getClass() /* pAddr */);

    public boolean send(MemorySegment handle, MemorySegment pPacket, int packetLen, IntArray pSendLen, io.vproxy.windivert.pni.WinDivertAddress pAddr) {
        boolean RESULT;
        try {
            RESULT = (boolean) sendMH.invokeExact((MemorySegment) (handle == null ? MemorySegment.NULL : handle), (MemorySegment) (pPacket == null ? MemorySegment.NULL : pPacket), packetLen, (MemorySegment) (pSendLen == null ? MemorySegment.NULL : pSendLen.MEMORY), (MemorySegment) (pAddr == null ? MemorySegment.NULL : pAddr.MEMORY));
        } catch (Throwable THROWABLE) {
            throw PanamaUtils.convertInvokeExactException(THROWABLE);
        }
        return RESULT;
    }

    private static final MethodHandle closeMH = PanamaUtils.lookupPNICriticalFunction(new PNILinkOptions(), boolean.class, "WinDivertClose", MemorySegment.class /* handle */);

    public boolean close(MemorySegment handle) {
        boolean RESULT;
        try {
            RESULT = (boolean) closeMH.invokeExact((MemorySegment) (handle == null ? MemorySegment.NULL : handle));
        } catch (Throwable THROWABLE) {
            throw PanamaUtils.convertInvokeExactException(THROWABLE);
        }
        return RESULT;
    }
}
// metadata.generator-version: pni 21.0.0.18
// sha256:9af3efa735cf8ad43b5ac37abb1dcb7403f38d8d8920f1535ec2f3de3dd8c822
