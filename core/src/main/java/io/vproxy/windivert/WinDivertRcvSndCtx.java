package io.vproxy.windivert;

import io.vproxy.pni.Allocator;
import io.vproxy.pni.array.IntArray;
import io.vproxy.windivert.pni.WinDivertAddress;

import java.lang.foreign.MemorySegment;

public class WinDivertRcvSndCtx {
    public final MemorySegment buf;
    public final IntArray plen;
    public final WinDivertAddress addr;

    // would be modified on rcv
    protected long recordedTimestampTicks = 0;

    public WinDivertRcvSndCtx(Allocator allocator) {
        this(allocator, 4096);
    }

    public WinDivertRcvSndCtx(Allocator allocator, long bufSize) {
        this.buf = allocator.allocate(bufSize);
        this.plen = new IntArray(allocator, 1);
        this.addr = new WinDivertAddress(allocator);
    }

    public void setRecordedTimestampTicks(long ticks) {
        this.recordedTimestampTicks = ticks;
    }

    public void clearRecordedTimestampTicks() {
        setRecordedTimestampTicks(0);
    }

    public long getDeltaTimestampTicks() {
        return addr.getTimestamp() - recordedTimestampTicks;
    }

    public long getDeltaTimestampMillis() {
        return getDeltaTimestampTicks() * 1000 / ProfileAPI.getPerformanceFrequency();
    }

    public long getDeltaTimestampNanos() {
        return getDeltaTimestampTicks() * 1_000_000_000L / ProfileAPI.getPerformanceFrequency();
    }
}
