package io.vproxy.windivert.pni;

import io.vproxy.pni.*;
import io.vproxy.pni.array.*;
import java.lang.foreign.*;
import java.lang.invoke.*;
import java.nio.ByteBuffer;

public class WinDivertDataSocket extends AbstractNativeObject implements NativeObject {
    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_LONG_UNALIGNED.withName("endpointId"),
        ValueLayout.JAVA_LONG_UNALIGNED.withName("parentEndpointId"),
        ValueLayout.JAVA_INT_UNALIGNED.withName("processId"),
        MemoryLayout.sequenceLayout(4L, ValueLayout.JAVA_INT_UNALIGNED).withName("localAddr"),
        MemoryLayout.sequenceLayout(4L, ValueLayout.JAVA_INT_UNALIGNED).withName("remoteAddr"),
        ValueLayout.JAVA_SHORT_UNALIGNED.withName("localPort"),
        ValueLayout.JAVA_SHORT_UNALIGNED.withName("remotePort"),
        ValueLayout.JAVA_BYTE.withName("protocol"),
        MemoryLayout.sequenceLayout(7L, ValueLayout.JAVA_BYTE) /* padding */
    );
    public final MemorySegment MEMORY;

    @Override
    public MemorySegment MEMORY() {
        return MEMORY;
    }

    private static final VarHandle endpointIdVH = LAYOUT.varHandle(
        MemoryLayout.PathElement.groupElement("endpointId")
    );

    public long getEndpointId() {
        return (long) endpointIdVH.get(MEMORY);
    }

    public void setEndpointId(long endpointId) {
        endpointIdVH.set(MEMORY, endpointId);
    }

    private static final VarHandle parentEndpointIdVH = LAYOUT.varHandle(
        MemoryLayout.PathElement.groupElement("parentEndpointId")
    );

    public long getParentEndpointId() {
        return (long) parentEndpointIdVH.get(MEMORY);
    }

    public void setParentEndpointId(long parentEndpointId) {
        parentEndpointIdVH.set(MEMORY, parentEndpointId);
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

    private final IntArray localAddr;

    public IntArray getLocalAddr() {
        return this.localAddr;
    }

    private final IntArray remoteAddr;

    public IntArray getRemoteAddr() {
        return this.remoteAddr;
    }

    private static final VarHandle localPortVH = LAYOUT.varHandle(
        MemoryLayout.PathElement.groupElement("localPort")
    );

    public short getLocalPort() {
        return (short) localPortVH.get(MEMORY);
    }

    public void setLocalPort(short localPort) {
        localPortVH.set(MEMORY, localPort);
    }

    private static final VarHandle remotePortVH = LAYOUT.varHandle(
        MemoryLayout.PathElement.groupElement("remotePort")
    );

    public short getRemotePort() {
        return (short) remotePortVH.get(MEMORY);
    }

    public void setRemotePort(short remotePort) {
        remotePortVH.set(MEMORY, remotePort);
    }

    private static final VarHandle protocolVH = LAYOUT.varHandle(
        MemoryLayout.PathElement.groupElement("protocol")
    );

    public byte getProtocol() {
        return (byte) protocolVH.get(MEMORY);
    }

    public void setProtocol(byte protocol) {
        protocolVH.set(MEMORY, protocol);
    }

    public WinDivertDataSocket(MemorySegment MEMORY) {
        MEMORY = MEMORY.reinterpret(LAYOUT.byteSize());
        this.MEMORY = MEMORY;
        long OFFSET = 0;
        OFFSET += ValueLayout.JAVA_LONG_UNALIGNED.byteSize();
        OFFSET += ValueLayout.JAVA_LONG_UNALIGNED.byteSize();
        OFFSET += ValueLayout.JAVA_INT_UNALIGNED.byteSize();
        this.localAddr = new IntArray(MEMORY.asSlice(OFFSET, 4 * ValueLayout.JAVA_INT_UNALIGNED.byteSize()));
        OFFSET += 4 * ValueLayout.JAVA_INT_UNALIGNED.byteSize();
        this.remoteAddr = new IntArray(MEMORY.asSlice(OFFSET, 4 * ValueLayout.JAVA_INT_UNALIGNED.byteSize()));
        OFFSET += 4 * ValueLayout.JAVA_INT_UNALIGNED.byteSize();
        OFFSET += ValueLayout.JAVA_SHORT_UNALIGNED.byteSize();
        OFFSET += ValueLayout.JAVA_SHORT_UNALIGNED.byteSize();
        OFFSET += ValueLayout.JAVA_BYTE.byteSize();
        OFFSET += 7; /* padding */
    }

    public WinDivertDataSocket(Allocator ALLOCATOR) {
        this(ALLOCATOR.allocate(LAYOUT));
    }

    @Override
    public void toString(StringBuilder SB, int INDENT, java.util.Set<NativeObjectTuple> VISITED, boolean CORRUPTED_MEMORY) {
        if (!VISITED.add(new NativeObjectTuple(this))) {
            SB.append("<...>@").append(Long.toString(MEMORY.address(), 16));
            return;
        }
        SB.append("WinDivertDataSocket{\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("endpointId => ");
            SB.append(getEndpointId());
        }
        SB.append(",\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("parentEndpointId => ");
            SB.append(getParentEndpointId());
        }
        SB.append(",\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("processId => ");
            SB.append(getProcessId());
        }
        SB.append(",\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("localAddr => ");
            PanamaUtils.nativeObjectToString(getLocalAddr(), SB, INDENT + 4, VISITED, CORRUPTED_MEMORY);
        }
        SB.append(",\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("remoteAddr => ");
            PanamaUtils.nativeObjectToString(getRemoteAddr(), SB, INDENT + 4, VISITED, CORRUPTED_MEMORY);
        }
        SB.append(",\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("localPort => ");
            SB.append(getLocalPort());
        }
        SB.append(",\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("remotePort => ");
            SB.append(getRemotePort());
        }
        SB.append(",\n");
        {
            SB.append(" ".repeat(INDENT + 4)).append("protocol => ");
            SB.append(getProtocol());
        }
        SB.append("\n");
        SB.append(" ".repeat(INDENT)).append("}@").append(Long.toString(MEMORY.address(), 16));
    }

    public static class Array extends RefArray<WinDivertDataSocket> {
        public Array(MemorySegment buf) {
            super(buf, WinDivertDataSocket.LAYOUT);
        }

        public Array(Allocator allocator, long len) {
            super(allocator, WinDivertDataSocket.LAYOUT, len);
        }

        public Array(PNIBuf buf) {
            super(buf, WinDivertDataSocket.LAYOUT);
        }

        @Override
        protected void elementToString(io.vproxy.windivert.pni.WinDivertDataSocket ELEM, StringBuilder SB, int INDENT, java.util.Set<NativeObjectTuple> VISITED, boolean CORRUPTED_MEMORY) {
            ELEM.toString(SB, INDENT, VISITED, CORRUPTED_MEMORY);
        }

        @Override
        protected String toStringTypeName() {
            return "WinDivertDataSocket.Array";
        }

        @Override
        protected WinDivertDataSocket construct(MemorySegment seg) {
            return new WinDivertDataSocket(seg);
        }

        @Override
        protected MemorySegment getSegment(WinDivertDataSocket value) {
            return value.MEMORY;
        }
    }

    public static class Func extends PNIFunc<WinDivertDataSocket> {
        private Func(io.vproxy.pni.CallSite<WinDivertDataSocket> func) {
            super(func);
        }

        private Func(io.vproxy.pni.CallSite<WinDivertDataSocket> func, Options opts) {
            super(func, opts);
        }

        private Func(MemorySegment MEMORY) {
            super(MEMORY);
        }

        public static Func of(io.vproxy.pni.CallSite<WinDivertDataSocket> func) {
            return new Func(func);
        }

        public static Func of(io.vproxy.pni.CallSite<WinDivertDataSocket> func, Options opts) {
            return new Func(func, opts);
        }

        public static Func of(MemorySegment MEMORY) {
            return new Func(MEMORY);
        }

        @Override
        protected String toStringTypeName() {
            return "WinDivertDataSocket.Func";
        }

        @Override
        protected WinDivertDataSocket construct(MemorySegment seg) {
            return new WinDivertDataSocket(seg);
        }
    }
}
// metadata.generator-version: pni 21.0.0.18
// sha256:1e9ddf171c59d7170f16502827adb57e17a5f10f5ae036370abf73887a1af1a5
