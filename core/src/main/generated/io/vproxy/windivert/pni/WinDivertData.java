package io.vproxy.windivert.pni;

import io.vproxy.pni.*;
import io.vproxy.pni.array.*;
import java.lang.foreign.*;
import java.lang.invoke.*;
import java.nio.ByteBuffer;

public class WinDivertData extends AbstractNativeObject implements NativeObject {
    public static final MemoryLayout LAYOUT = MemoryLayout.unionLayout(
        io.vproxy.windivert.pni.WinDivertDataNetwork.LAYOUT.withName("network"),
        io.vproxy.windivert.pni.WinDivertDataFlow.LAYOUT.withName("flow"),
        io.vproxy.windivert.pni.WinDivertDataSocket.LAYOUT.withName("socket"),
        io.vproxy.windivert.pni.WinDivertDataReflect.LAYOUT.withName("reflect"),
        MemoryLayout.sequenceLayout(64L, ValueLayout.JAVA_BYTE).withName("reserved0")
    );
    public final MemorySegment MEMORY;

    @Override
    public MemorySegment MEMORY() {
        return MEMORY;
    }

    private final io.vproxy.windivert.pni.WinDivertDataNetwork network;

    public io.vproxy.windivert.pni.WinDivertDataNetwork getNetwork() {
        return this.network;
    }

    private final io.vproxy.windivert.pni.WinDivertDataFlow flow;

    public io.vproxy.windivert.pni.WinDivertDataFlow getFlow() {
        return this.flow;
    }

    private final io.vproxy.windivert.pni.WinDivertDataSocket socket;

    public io.vproxy.windivert.pni.WinDivertDataSocket getSocket() {
        return this.socket;
    }

    private final io.vproxy.windivert.pni.WinDivertDataReflect reflect;

    public io.vproxy.windivert.pni.WinDivertDataReflect getReflect() {
        return this.reflect;
    }

    private final MemorySegment reserved0;

    public MemorySegment getReserved0() {
        return this.reserved0;
    }

    public WinDivertData(MemorySegment MEMORY) {
        MEMORY = MEMORY.reinterpret(LAYOUT.byteSize());
        this.MEMORY = MEMORY;
        long OFFSET = 0;
        this.network = new io.vproxy.windivert.pni.WinDivertDataNetwork(MEMORY.asSlice(OFFSET, io.vproxy.windivert.pni.WinDivertDataNetwork.LAYOUT.byteSize()));
        OFFSET += io.vproxy.windivert.pni.WinDivertDataNetwork.LAYOUT.byteSize();
        OFFSET = 0;
        this.flow = new io.vproxy.windivert.pni.WinDivertDataFlow(MEMORY.asSlice(OFFSET, io.vproxy.windivert.pni.WinDivertDataFlow.LAYOUT.byteSize()));
        OFFSET += io.vproxy.windivert.pni.WinDivertDataFlow.LAYOUT.byteSize();
        OFFSET = 0;
        this.socket = new io.vproxy.windivert.pni.WinDivertDataSocket(MEMORY.asSlice(OFFSET, io.vproxy.windivert.pni.WinDivertDataSocket.LAYOUT.byteSize()));
        OFFSET += io.vproxy.windivert.pni.WinDivertDataSocket.LAYOUT.byteSize();
        OFFSET = 0;
        this.reflect = new io.vproxy.windivert.pni.WinDivertDataReflect(MEMORY.asSlice(OFFSET, io.vproxy.windivert.pni.WinDivertDataReflect.LAYOUT.byteSize()));
        OFFSET += io.vproxy.windivert.pni.WinDivertDataReflect.LAYOUT.byteSize();
        OFFSET = 0;
        this.reserved0 = MEMORY.asSlice(OFFSET, 64);
        OFFSET += 64;
        OFFSET = 0;
    }

    public WinDivertData(Allocator ALLOCATOR) {
        this(ALLOCATOR.allocate(LAYOUT));
    }

    @Override
    public void toString(StringBuilder SB, int INDENT, java.util.Set<NativeObjectTuple> VISITED, boolean CORRUPTED_MEMORY) {
        if (!VISITED.add(new NativeObjectTuple(this))) {
            SB.append("<...>@").append(Long.toString(MEMORY.address(), 16));
            return;
        }
        CORRUPTED_MEMORY = true;
        SB.append("WinDivertData(\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("network => ");
            PanamaUtils.nativeObjectToString(getNetwork(), SB, INDENT + 4, VISITED, CORRUPTED_MEMORY);
        }
        SB.append(",\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("flow => ");
            PanamaUtils.nativeObjectToString(getFlow(), SB, INDENT + 4, VISITED, CORRUPTED_MEMORY);
        }
        SB.append(",\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("socket => ");
            PanamaUtils.nativeObjectToString(getSocket(), SB, INDENT + 4, VISITED, CORRUPTED_MEMORY);
        }
        SB.append(",\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("reflect => ");
            PanamaUtils.nativeObjectToString(getReflect(), SB, INDENT + 4, VISITED, CORRUPTED_MEMORY);
        }
        SB.append(",\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("reserved0 => ");
            SB.append(PanamaUtils.memorySegmentToString(getReserved0()));
        }
        SB.append("\n");
        SB.append(" ".repeat(INDENT)).append(")@").append(Long.toString(MEMORY.address(), 16));
    }

    public static class Array extends RefArray<WinDivertData> {
        public Array(MemorySegment buf) {
            super(buf, WinDivertData.LAYOUT);
        }

        public Array(Allocator allocator, long len) {
            super(allocator, WinDivertData.LAYOUT, len);
        }

        public Array(PNIBuf buf) {
            super(buf, WinDivertData.LAYOUT);
        }

        @Override
        protected void elementToString(io.vproxy.windivert.pni.WinDivertData ELEM, StringBuilder SB, int INDENT, java.util.Set<NativeObjectTuple> VISITED, boolean CORRUPTED_MEMORY) {
            ELEM.toString(SB, INDENT, VISITED, CORRUPTED_MEMORY);
        }

        @Override
        protected String toStringTypeName() {
            return "WinDivertData.Array";
        }

        @Override
        protected WinDivertData construct(MemorySegment seg) {
            return new WinDivertData(seg);
        }

        @Override
        protected MemorySegment getSegment(WinDivertData value) {
            return value.MEMORY;
        }
    }

    public static class Func extends PNIFunc<WinDivertData> {
        private Func(io.vproxy.pni.CallSite<WinDivertData> func) {
            super(func);
        }

        private Func(io.vproxy.pni.CallSite<WinDivertData> func, Options opts) {
            super(func, opts);
        }

        private Func(MemorySegment MEMORY) {
            super(MEMORY);
        }

        public static Func of(io.vproxy.pni.CallSite<WinDivertData> func) {
            return new Func(func);
        }

        public static Func of(io.vproxy.pni.CallSite<WinDivertData> func, Options opts) {
            return new Func(func, opts);
        }

        public static Func of(MemorySegment MEMORY) {
            return new Func(MEMORY);
        }

        @Override
        protected String toStringTypeName() {
            return "WinDivertData.Func";
        }

        @Override
        protected WinDivertData construct(MemorySegment seg) {
            return new WinDivertData(seg);
        }
    }
}
// metadata.generator-version: pni 21.0.0.18
// sha256:2909dc394a9ca06f24e4a7c30b90d3ed88b152ff1b7e9ff8a579982de9690eec
