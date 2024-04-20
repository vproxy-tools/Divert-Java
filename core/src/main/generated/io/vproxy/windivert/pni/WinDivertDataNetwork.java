package io.vproxy.windivert.pni;

import io.vproxy.pni.*;
import io.vproxy.pni.array.*;
import java.lang.foreign.*;
import java.lang.invoke.*;
import java.nio.ByteBuffer;

public class WinDivertDataNetwork extends AbstractNativeObject implements NativeObject {
    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_INT_UNALIGNED.withName("ifIdx"),
        ValueLayout.JAVA_INT_UNALIGNED.withName("subIfIdx")
    );
    public final MemorySegment MEMORY;

    @Override
    public MemorySegment MEMORY() {
        return MEMORY;
    }

    private static final VarHandle ifIdxVH = LAYOUT.varHandle(
        MemoryLayout.PathElement.groupElement("ifIdx")
    );

    public int getIfIdx() {
        return (int) ifIdxVH.get(MEMORY);
    }

    public void setIfIdx(int ifIdx) {
        ifIdxVH.set(MEMORY, ifIdx);
    }

    private static final VarHandle subIfIdxVH = LAYOUT.varHandle(
        MemoryLayout.PathElement.groupElement("subIfIdx")
    );

    public int getSubIfIdx() {
        return (int) subIfIdxVH.get(MEMORY);
    }

    public void setSubIfIdx(int subIfIdx) {
        subIfIdxVH.set(MEMORY, subIfIdx);
    }

    public WinDivertDataNetwork(MemorySegment MEMORY) {
        MEMORY = MEMORY.reinterpret(LAYOUT.byteSize());
        this.MEMORY = MEMORY;
        long OFFSET = 0;
        OFFSET += ValueLayout.JAVA_INT_UNALIGNED.byteSize();
        OFFSET += ValueLayout.JAVA_INT_UNALIGNED.byteSize();
    }

    public WinDivertDataNetwork(Allocator ALLOCATOR) {
        this(ALLOCATOR.allocate(LAYOUT));
    }

    @Override
    public void toString(StringBuilder SB, int INDENT, java.util.Set<NativeObjectTuple> VISITED, boolean CORRUPTED_MEMORY) {
        if (!VISITED.add(new NativeObjectTuple(this))) {
            SB.append("<...>@").append(Long.toString(MEMORY.address(), 16));
            return;
        }
        SB.append("WinDivertDataNetwork{\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("ifIdx => ");
            SB.append(getIfIdx());
        }
        SB.append(",\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("subIfIdx => ");
            SB.append(getSubIfIdx());
        }
        SB.append("\n");
        SB.append(" ".repeat(INDENT)).append("}@").append(Long.toString(MEMORY.address(), 16));
    }

    public static class Array extends RefArray<WinDivertDataNetwork> {
        public Array(MemorySegment buf) {
            super(buf, WinDivertDataNetwork.LAYOUT);
        }

        public Array(Allocator allocator, long len) {
            super(allocator, WinDivertDataNetwork.LAYOUT, len);
        }

        public Array(PNIBuf buf) {
            super(buf, WinDivertDataNetwork.LAYOUT);
        }

        @Override
        protected void elementToString(io.vproxy.windivert.pni.WinDivertDataNetwork ELEM, StringBuilder SB, int INDENT, java.util.Set<NativeObjectTuple> VISITED, boolean CORRUPTED_MEMORY) {
            ELEM.toString(SB, INDENT, VISITED, CORRUPTED_MEMORY);
        }

        @Override
        protected String toStringTypeName() {
            return "WinDivertDataNetwork.Array";
        }

        @Override
        protected WinDivertDataNetwork construct(MemorySegment seg) {
            return new WinDivertDataNetwork(seg);
        }

        @Override
        protected MemorySegment getSegment(WinDivertDataNetwork value) {
            return value.MEMORY;
        }
    }

    public static class Func extends PNIFunc<WinDivertDataNetwork> {
        private Func(io.vproxy.pni.CallSite<WinDivertDataNetwork> func) {
            super(func);
        }

        private Func(io.vproxy.pni.CallSite<WinDivertDataNetwork> func, Options opts) {
            super(func, opts);
        }

        private Func(MemorySegment MEMORY) {
            super(MEMORY);
        }

        public static Func of(io.vproxy.pni.CallSite<WinDivertDataNetwork> func) {
            return new Func(func);
        }

        public static Func of(io.vproxy.pni.CallSite<WinDivertDataNetwork> func, Options opts) {
            return new Func(func, opts);
        }

        public static Func of(MemorySegment MEMORY) {
            return new Func(MEMORY);
        }

        @Override
        protected String toStringTypeName() {
            return "WinDivertDataNetwork.Func";
        }

        @Override
        protected WinDivertDataNetwork construct(MemorySegment seg) {
            return new WinDivertDataNetwork(seg);
        }
    }
}
// metadata.generator-version: pni 21.0.0.18
// sha256:14bdb6f61fd6e94f46476cf6a7ace3a6524cd1ae6c9d45cde18b80ac77946abf
