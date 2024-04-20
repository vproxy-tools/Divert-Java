package io.vproxy.windivert;

import io.vproxy.pni.Allocator;
import io.vproxy.pni.array.IntArray;
import io.vproxy.windivert.pni.WinDivertAddress;

import java.lang.foreign.MemorySegment;

public class WinDivertRcvSndCtx {
    public final MemorySegment buf;
    public final IntArray plen;
    public final WinDivertAddress addr;

    public WinDivertRcvSndCtx(Allocator allocator) {
        this(allocator, 4096);
    }

    public WinDivertRcvSndCtx(Allocator allocator, long bufSize) {
        this.buf = allocator.allocate(bufSize);
        this.plen = new IntArray(allocator, 1);
        this.addr = new WinDivertAddress(allocator);
    }
}
