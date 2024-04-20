package io.vproxy.windivert.pni;

import io.vproxy.pni.*;
import io.vproxy.pni.array.*;
import java.lang.foreign.*;
import java.lang.invoke.*;
import java.nio.ByteBuffer;

public class WinDivertDataReflect extends AbstractNativeObject implements NativeObject {
    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_LONG_UNALIGNED.withName("timestamp"),
        ValueLayout.JAVA_INT_UNALIGNED.withName("processId"),
        ValueLayout.JAVA_INT_UNALIGNED.withName("layer"),
        ValueLayout.JAVA_LONG_UNALIGNED.withName("flags"),
        ValueLayout.JAVA_SHORT_UNALIGNED.withName("priority"),
        MemoryLayout.sequenceLayout(6L, ValueLayout.JAVA_BYTE) /* padding */
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

    private static final VarHandle processIdVH = LAYOUT.varHandle(
        MemoryLayout.PathElement.groupElement("processId")
    );

    public int getProcessId() {
        return (int) processIdVH.get(MEMORY);
    }

    public void setProcessId(int processId) {
        processIdVH.set(MEMORY, processId);
    }

    private static final VarHandle layerVH = LAYOUT.varHandle(
        MemoryLayout.PathElement.groupElement("layer")
    );

    public int getLayer() {
        return (int) layerVH.get(MEMORY);
    }

    public void setLayer(int layer) {
        layerVH.set(MEMORY, layer);
    }

    private static final VarHandle flagsVH = LAYOUT.varHandle(
        MemoryLayout.PathElement.groupElement("flags")
    );

    public long getFlags() {
        return (long) flagsVH.get(MEMORY);
    }

    public void setFlags(long flags) {
        flagsVH.set(MEMORY, flags);
    }

    private static final VarHandle priorityVH = LAYOUT.varHandle(
        MemoryLayout.PathElement.groupElement("priority")
    );

    public short getPriority() {
        return (short) priorityVH.get(MEMORY);
    }

    public void setPriority(short priority) {
        priorityVH.set(MEMORY, priority);
    }

    public WinDivertDataReflect(MemorySegment MEMORY) {
        MEMORY = MEMORY.reinterpret(LAYOUT.byteSize());
        this.MEMORY = MEMORY;
        long OFFSET = 0;
        OFFSET += ValueLayout.JAVA_LONG_UNALIGNED.byteSize();
        OFFSET += ValueLayout.JAVA_INT_UNALIGNED.byteSize();
        OFFSET += ValueLayout.JAVA_INT_UNALIGNED.byteSize();
        OFFSET += ValueLayout.JAVA_LONG_UNALIGNED.byteSize();
        OFFSET += ValueLayout.JAVA_SHORT_UNALIGNED.byteSize();
        OFFSET += 6; /* padding */
    }

    public WinDivertDataReflect(Allocator ALLOCATOR) {
        this(ALLOCATOR.allocate(LAYOUT));
    }

    @Override
    public void toString(StringBuilder SB, int INDENT, java.util.Set<NativeObjectTuple> VISITED, boolean CORRUPTED_MEMORY) {
        if (!VISITED.add(new NativeObjectTuple(this))) {
            SB.append("<...>@").append(Long.toString(MEMORY.address(), 16));
            return;
        }
        SB.append("WinDivertDataReflect{\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("timestamp => ");
            SB.append(getTimestamp());
        }
        SB.append(",\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("processId => ");
            SB.append(getProcessId());
        }
        SB.append(",\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("layer => ");
            SB.append(getLayer());
        }
        SB.append(",\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("flags => ");
            SB.append(getFlags());
        }
        SB.append(",\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("priority => ");
            SB.append(getPriority());
        }
        SB.append("\n");
        SB.append(" ".repeat(INDENT)).append("}@").append(Long.toString(MEMORY.address(), 16));
    }

    public static class Array extends RefArray<WinDivertDataReflect> {
        public Array(MemorySegment buf) {
            super(buf, WinDivertDataReflect.LAYOUT);
        }

        public Array(Allocator allocator, long len) {
            super(allocator, WinDivertDataReflect.LAYOUT, len);
        }

        public Array(PNIBuf buf) {
            super(buf, WinDivertDataReflect.LAYOUT);
        }

        @Override
        protected void elementToString(io.vproxy.windivert.pni.WinDivertDataReflect ELEM, StringBuilder SB, int INDENT, java.util.Set<NativeObjectTuple> VISITED, boolean CORRUPTED_MEMORY) {
            ELEM.toString(SB, INDENT, VISITED, CORRUPTED_MEMORY);
        }

        @Override
        protected String toStringTypeName() {
            return "WinDivertDataReflect.Array";
        }

        @Override
        protected WinDivertDataReflect construct(MemorySegment seg) {
            return new WinDivertDataReflect(seg);
        }

        @Override
        protected MemorySegment getSegment(WinDivertDataReflect value) {
            return value.MEMORY;
        }
    }

    public static class Func extends PNIFunc<WinDivertDataReflect> {
        private Func(io.vproxy.pni.CallSite<WinDivertDataReflect> func) {
            super(func);
        }

        private Func(io.vproxy.pni.CallSite<WinDivertDataReflect> func, Options opts) {
            super(func, opts);
        }

        private Func(MemorySegment MEMORY) {
            super(MEMORY);
        }

        public static Func of(io.vproxy.pni.CallSite<WinDivertDataReflect> func) {
            return new Func(func);
        }

        public static Func of(io.vproxy.pni.CallSite<WinDivertDataReflect> func, Options opts) {
            return new Func(func, opts);
        }

        public static Func of(MemorySegment MEMORY) {
            return new Func(MEMORY);
        }

        @Override
        protected String toStringTypeName() {
            return "WinDivertDataReflect.Func";
        }

        @Override
        protected WinDivertDataReflect construct(MemorySegment seg) {
            return new WinDivertDataReflect(seg);
        }
    }
}
// metadata.generator-version: pni 21.0.0.18
// sha256:d72da937987a0d3416298ace827f0a1187049c3a961515f5a04b34400edd5633
