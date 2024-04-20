package io.vproxy.windivert.pni;

import io.vproxy.pni.*;
import io.vproxy.pni.array.*;
import java.lang.foreign.*;
import java.lang.invoke.*;
import java.nio.ByteBuffer;

public class WinDivertAddress extends AbstractNativeObject implements NativeObject {
    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_LONG_UNALIGNED.withName("timestamp"),
        ValueLayout.JAVA_INT_UNALIGNED.withName("bitfield0"),
        ValueLayout.JAVA_INT_UNALIGNED.withName("bitfield1"),
        io.vproxy.windivert.pni.WinDivertData.LAYOUT.withName("union0")
    );
    public final MemorySegment MEMORY;

    @Override
    public MemorySegment MEMORY() {
        return MEMORY;
    }

    private static final VarHandle timestampVH = LAYOUT.varHandle(
        MemoryLayout.PathElement.groupElement("timestamp")
    );

    public long getTimestamp() {
        return (long) timestampVH.get(MEMORY);
    }

    public void setTimestamp(long timestamp) {
        timestampVH.set(MEMORY, timestamp);
    }

    private static final VarHandle bitfield0VH = LAYOUT.varHandle(
        MemoryLayout.PathElement.groupElement("bitfield0")
    );

    public int getBitfield0() {
        return (int) bitfield0VH.get(MEMORY);
    }

    public void setBitfield0(int bitfield0) {
        bitfield0VH.set(MEMORY, bitfield0);
    }

    public int getLayer() {
        var N = getBitfield0();
        return (int) ((N >> 0) & 0b11111111);
    }

    public void setLayer(int layer) {
        var N = getBitfield0();
        int MASK = (int) (0b11111111 << 0);
        layer = (int) (layer & 0b11111111);
        layer = (int) (layer << 0);
        N = (int) ((N & ~MASK) | (layer & MASK));
        setBitfield0(N);
    }

    public int getEvent() {
        var N = getBitfield0();
        return (int) ((N >> 8) & 0b11111111);
    }

    public void setEvent(int event) {
        var N = getBitfield0();
        int MASK = (int) (0b11111111 << 8);
        event = (int) (event & 0b11111111);
        event = (int) (event << 8);
        N = (int) ((N & ~MASK) | (event & MASK));
        setBitfield0(N);
    }

    public boolean isSniffed() {
        var N = getBitfield0();
        return ((N >> 16) & 0b1) == 1;
    }

    public void setSniffed(boolean sniffed) {
        var N = getBitfield0();
        int MASK = (int) (0b1 << 16);
        var NN = (int) (sniffed ? 1 : 0);
        NN = (int) (NN << 16);
        N = (int) ((N & ~MASK) | (NN & MASK));
        setBitfield0(N);
    }

    public boolean isOutbound() {
        var N = getBitfield0();
        return ((N >> 17) & 0b1) == 1;
    }

    public void setOutbound(boolean outbound) {
        var N = getBitfield0();
        int MASK = (int) (0b1 << 17);
        var NN = (int) (outbound ? 1 : 0);
        NN = (int) (NN << 17);
        N = (int) ((N & ~MASK) | (NN & MASK));
        setBitfield0(N);
    }

    public boolean isLoopback() {
        var N = getBitfield0();
        return ((N >> 18) & 0b1) == 1;
    }

    public void setLoopback(boolean loopback) {
        var N = getBitfield0();
        int MASK = (int) (0b1 << 18);
        var NN = (int) (loopback ? 1 : 0);
        NN = (int) (NN << 18);
        N = (int) ((N & ~MASK) | (NN & MASK));
        setBitfield0(N);
    }

    public boolean isImpostor() {
        var N = getBitfield0();
        return ((N >> 19) & 0b1) == 1;
    }

    public void setImpostor(boolean impostor) {
        var N = getBitfield0();
        int MASK = (int) (0b1 << 19);
        var NN = (int) (impostor ? 1 : 0);
        NN = (int) (NN << 19);
        N = (int) ((N & ~MASK) | (NN & MASK));
        setBitfield0(N);
    }

    public boolean isIpv6() {
        var N = getBitfield0();
        return ((N >> 20) & 0b1) == 1;
    }

    public void setIpv6(boolean ipv6) {
        var N = getBitfield0();
        int MASK = (int) (0b1 << 20);
        var NN = (int) (ipv6 ? 1 : 0);
        NN = (int) (NN << 20);
        N = (int) ((N & ~MASK) | (NN & MASK));
        setBitfield0(N);
    }

    public boolean isIpChecksum() {
        var N = getBitfield0();
        return ((N >> 21) & 0b1) == 1;
    }

    public void setIpChecksum(boolean ipChecksum) {
        var N = getBitfield0();
        int MASK = (int) (0b1 << 21);
        var NN = (int) (ipChecksum ? 1 : 0);
        NN = (int) (NN << 21);
        N = (int) ((N & ~MASK) | (NN & MASK));
        setBitfield0(N);
    }

    public boolean isTcpChecksum() {
        var N = getBitfield0();
        return ((N >> 22) & 0b1) == 1;
    }

    public void setTcpChecksum(boolean tcpChecksum) {
        var N = getBitfield0();
        int MASK = (int) (0b1 << 22);
        var NN = (int) (tcpChecksum ? 1 : 0);
        NN = (int) (NN << 22);
        N = (int) ((N & ~MASK) | (NN & MASK));
        setBitfield0(N);
    }

    public boolean isUdpChecksum() {
        var N = getBitfield0();
        return ((N >> 23) & 0b1) == 1;
    }

    public void setUdpChecksum(boolean udpChecksum) {
        var N = getBitfield0();
        int MASK = (int) (0b1 << 23);
        var NN = (int) (udpChecksum ? 1 : 0);
        NN = (int) (NN << 23);
        N = (int) ((N & ~MASK) | (NN & MASK));
        setBitfield0(N);
    }

    private static final VarHandle bitfield1VH = LAYOUT.varHandle(
        MemoryLayout.PathElement.groupElement("bitfield1")
    );

    public int getBitfield1() {
        return (int) bitfield1VH.get(MEMORY);
    }

    public void setBitfield1(int bitfield1) {
        bitfield1VH.set(MEMORY, bitfield1);
    }

    private final io.vproxy.windivert.pni.WinDivertData union0;

    public io.vproxy.windivert.pni.WinDivertData getUnion0() {
        return this.union0;
    }

    public WinDivertAddress(MemorySegment MEMORY) {
        MEMORY = MEMORY.reinterpret(LAYOUT.byteSize());
        this.MEMORY = MEMORY;
        long OFFSET = 0;
        OFFSET += ValueLayout.JAVA_LONG_UNALIGNED.byteSize();
        OFFSET += ValueLayout.JAVA_INT_UNALIGNED.byteSize();
        OFFSET += ValueLayout.JAVA_INT_UNALIGNED.byteSize();
        this.union0 = new io.vproxy.windivert.pni.WinDivertData(MEMORY.asSlice(OFFSET, io.vproxy.windivert.pni.WinDivertData.LAYOUT.byteSize()));
        OFFSET += io.vproxy.windivert.pni.WinDivertData.LAYOUT.byteSize();
    }

    public WinDivertAddress(Allocator ALLOCATOR) {
        this(ALLOCATOR.allocate(LAYOUT));
    }

    @Override
    public void toString(StringBuilder SB, int INDENT, java.util.Set<NativeObjectTuple> VISITED, boolean CORRUPTED_MEMORY) {
        if (!VISITED.add(new NativeObjectTuple(this))) {
            SB.append("<...>@").append(Long.toString(MEMORY.address(), 16));
            return;
        }
        SB.append("WinDivertAddress{\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("timestamp => ");
            SB.append(getTimestamp());
        }
        SB.append(",\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("bitfield0 => ");
            SB.append(getBitfield0());
            SB.append(" {\n");
            SB.append(" ".repeat(INDENT + 8)).append("layer:8 => ").append(getLayer());
            SB.append(",\n");
            SB.append(" ".repeat(INDENT + 8)).append("event:8 => ").append(getEvent());
            SB.append(",\n");
            SB.append(" ".repeat(INDENT + 8)).append("sniffed:1 => ").append(isSniffed());
            SB.append(",\n");
            SB.append(" ".repeat(INDENT + 8)).append("outbound:1 => ").append(isOutbound());
            SB.append(",\n");
            SB.append(" ".repeat(INDENT + 8)).append("loopback:1 => ").append(isLoopback());
            SB.append(",\n");
            SB.append(" ".repeat(INDENT + 8)).append("impostor:1 => ").append(isImpostor());
            SB.append(",\n");
            SB.append(" ".repeat(INDENT + 8)).append("ipv6:1 => ").append(isIpv6());
            SB.append(",\n");
            SB.append(" ".repeat(INDENT + 8)).append("ipChecksum:1 => ").append(isIpChecksum());
            SB.append(",\n");
            SB.append(" ".repeat(INDENT + 8)).append("tcpChecksum:1 => ").append(isTcpChecksum());
            SB.append(",\n");
            SB.append(" ".repeat(INDENT + 8)).append("udpChecksum:1 => ").append(isUdpChecksum());
            SB.append("\n");
            SB.append(" ".repeat(INDENT + 4)).append("}");
        }
        SB.append(",\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("bitfield1 => ");
            SB.append(getBitfield1());
        }
        SB.append(",\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("union0 => ");
            PanamaUtils.nativeObjectToString(getUnion0(), SB, INDENT + 4, VISITED, CORRUPTED_MEMORY);
        }
        SB.append("\n");
        SB.append(" ".repeat(INDENT)).append("}@").append(Long.toString(MEMORY.address(), 16));
    }

    public static class Array extends RefArray<WinDivertAddress> {
        public Array(MemorySegment buf) {
            super(buf, WinDivertAddress.LAYOUT);
        }

        public Array(Allocator allocator, long len) {
            super(allocator, WinDivertAddress.LAYOUT, len);
        }

        public Array(PNIBuf buf) {
            super(buf, WinDivertAddress.LAYOUT);
        }

        @Override
        protected void elementToString(io.vproxy.windivert.pni.WinDivertAddress ELEM, StringBuilder SB, int INDENT, java.util.Set<NativeObjectTuple> VISITED, boolean CORRUPTED_MEMORY) {
            ELEM.toString(SB, INDENT, VISITED, CORRUPTED_MEMORY);
        }

        @Override
        protected String toStringTypeName() {
            return "WinDivertAddress.Array";
        }

        @Override
        protected WinDivertAddress construct(MemorySegment seg) {
            return new WinDivertAddress(seg);
        }

        @Override
        protected MemorySegment getSegment(WinDivertAddress value) {
            return value.MEMORY;
        }
    }

    public static class Func extends PNIFunc<WinDivertAddress> {
        private Func(io.vproxy.pni.CallSite<WinDivertAddress> func) {
            super(func);
        }

        private Func(io.vproxy.pni.CallSite<WinDivertAddress> func, Options opts) {
            super(func, opts);
        }

        private Func(MemorySegment MEMORY) {
            super(MEMORY);
        }

        public static Func of(io.vproxy.pni.CallSite<WinDivertAddress> func) {
            return new Func(func);
        }

        public static Func of(io.vproxy.pni.CallSite<WinDivertAddress> func, Options opts) {
            return new Func(func, opts);
        }

        public static Func of(MemorySegment MEMORY) {
            return new Func(MEMORY);
        }

        @Override
        protected String toStringTypeName() {
            return "WinDivertAddress.Func";
        }

        @Override
        protected WinDivertAddress construct(MemorySegment seg) {
            return new WinDivertAddress(seg);
        }
    }
}
// metadata.generator-version: pni 21.0.0.18
// sha256:73a726034d9d5f8e18f174ea0e4a9d1c532cb83ea4cc9df0e85df9430ac559c7
