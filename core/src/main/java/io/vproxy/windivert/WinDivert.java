package io.vproxy.windivert;

import io.vproxy.base.util.LogType;
import io.vproxy.base.util.Logger;
import io.vproxy.base.util.OS;
import io.vproxy.base.util.Utils;
import io.vproxy.base.util.bytearray.MemorySegmentByteArray;
import io.vproxy.pni.Allocator;
import io.vproxy.pni.PNIString;
import io.vproxy.vpacket.AbstractIpPacket;
import io.vproxy.vpacket.Ipv4Packet;
import io.vproxy.vpacket.Ipv6Packet;
import io.vproxy.vpacket.PacketDataBuffer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.foreign.MemorySegment;

public class WinDivert {
    private static final String DRIVER_NAME = "vproxy_windivert";
    private static volatile boolean isLoaded = false;

    public static boolean isLoaded() {
        return isLoaded;
    }

    public static void load() throws UnsatisfiedLinkError {
        if (isLoaded) {
            return;
        }
        synchronized (WinDivert.class) {
            if (isLoaded) {
                return;
            }
            try {
                loadSys();
            } catch (Throwable t) {
                Logger.warn(LogType.SYS_ERROR, "failed to load driver, try to unload then reload", t);
                unloadSys();
                loadSys();
            }
            loadDLL();
            isLoaded = true;
        }
    }

    public static void unload() {
        if (!isLoaded) {
            return;
        }
        synchronized (WinDivert.class) {
            if (!isLoaded) {
                return;
            }
            var ok = unloadSys();
            if (ok) {
                isLoaded = false;
            }
        }
    }

    private static void loadSys() {
        if (isSysLoaded()) {
            startDriver();
            return;
        }

        File sysFile = null;
        try (var sysStream = WinDivert.class.getResourceAsStream(STR."/io/vproxy/windivert/WinDivert64-\{OS.arch()}.sys")) {
            if (sysStream == null) {
                Logger.warn(LogType.ALERT, STR."unable to find WinDivert64-\{OS.arch()}.sys, please make sure the driver is already loaded");
                return;
            }

            sysFile = File.createTempFile("WinDivert64", ".sys");
            try (var sysFileStream = new FileOutputStream(sysFile)) {
                sysStream.transferTo(sysFileStream);
            }
        } catch (IOException e) {
            Logger.error(LogType.FILE_ERROR, "failed to release WinDivert64.sys");
            if (sysFile != null) {
                //noinspection ResultOfMethodCallIgnored
                sysFile.delete();
            }
            throw new UnsatisfiedLinkError("failed to release WinDivert64.sys");
        }

        createDriver(sysFile.getAbsolutePath());
        startDriver();
    }

    private static boolean unloadSys() {
        boolean isLoaded;
        try {
            isLoaded = isSysLoaded();
        } catch (Throwable t) {
            return false;
        }
        if (!isLoaded) {
            return true;
        }
        try {
            Utils.execute(STR."sc.exe stop \{DRIVER_NAME}", true);
        } catch (Exception e) {
            Logger.error(LogType.SYS_ERROR, e.getMessage());
            return false;
        }
        Utils.ExecuteResult res;
        try {
            res = Utils.execute(STR."sc.exe delete \{DRIVER_NAME}", true);
        } catch (Exception e) {
            Logger.error(LogType.SYS_ERROR, e.getMessage());
            return false;
        }
        if (res.exitCode == 0) {
            return true;
        }
        Logger.error(LogType.SYS_ERROR, STR."failed to delete driver: exitCode: \{res.exitCode}\nstdout:\n\{res.stdout}\nstderr:\n\{res.stderr}");
        return false;
    }

    private static boolean isSysLoaded() {
        try {
            var res = Utils.execute(STR."sc.exe query \{DRIVER_NAME}", true);
            return res.exitCode == 0;
        } catch (Exception e) {
            throw new UnsatisfiedLinkError(STR."failed to query loaded drivers, \{Utils.formatErr(e)}");
        }
    }

    private static void createDriver(String file) {
        try {
            var res = Utils.execute(STR."sc.exe create \{DRIVER_NAME} binpath=\{Utils.escapePath(file)} type=kernel start=demand", true);
            if (res.exitCode != 0) {
                throw new UnsatisfiedLinkError(STR."failed to create driver, exitCode=\{res.exitCode}\nstdout:\n\{res.stdout}\nstderr:\n\{res.stderr}");
            }
        } catch (Exception e) {
            throw new UnsatisfiedLinkError(STR."failed to create driver, \{Utils.formatErr(e)}");
        }
    }

    private static void startDriver() {
        try {
            var res = Utils.execute(STR."sc.exe start \{DRIVER_NAME}", true);
            if (res.exitCode == 0) {
                return;
            }
            if (res.stdout.contains("An instance of the service is already running.")) {
                return;
            }
            throw new UnsatisfiedLinkError(STR."failed to start driver, exitCode=\{res.exitCode}\nstdout:\n\{res.stdout}\nstderr:\n\{res.stderr}");
        } catch (Exception e) {
            throw new UnsatisfiedLinkError(STR."failed to start driver, \{Utils.formatErr(e)}");
        }
    }

    private static void loadDLL() {
        Utils.loadDynamicLibrary("WinDivert", WinDivert.class.getClassLoader(), "io/vproxy/windivert/");
    }

    private final MemorySegment handle;
    private volatile boolean closed;

    private WinDivert(MemorySegment handle) {
        this.handle = handle;
    }

    public static WinDivert open(String filter) throws WinDivertException {
        return open(filter, WinDivertLayer.WINDIVERT_LAYER_NETWORK);
    }

    public static WinDivert open(String filter, WinDivertLayer layer) throws WinDivertException {
        return open(filter, layer, 0, 0);
    }

    public static WinDivert open(String filter, WinDivertLayer layer, int priority, long flags) throws WinDivertException {
        load();

        MemorySegment handle;
        try (var allocator = Allocator.ofConfined()) {
            var filterNative = new PNIString(allocator, filter);
            handle = io.vproxy.windivert.pni.WinDivert.get().open(filterNative, layer.value, (short) priority, flags);
            if (handle == null) {
                throw new WinDivertException("failed to open WinDivert");
            }
        }
        return new WinDivert(handle);
    }

    public MemorySegment getHandle() {
        return handle;
    }

    public MemorySegment receiveRaw(WinDivertRcvSndCtx ctx) throws WinDivertException {
        var ok = io.vproxy.windivert.pni.WinDivert.get().recv(handle, ctx.buf, (int) ctx.buf.byteSize(), ctx.plen, ctx.addr);
        if (!ok) {
            throw new WinDivertException(STR."failed to recv packet from \{this}");
        }
        if (ctx.plen.get(0) == 0)
            return null;
        return ctx.buf.reinterpret(ctx.plen.get(0));
    }

    public WinDivertPacketRecv receive(WinDivertRcvSndCtx ctx) throws WinDivertException {
        var pktBuf = receiveRaw(ctx);
        if (pktBuf == null) {
            return new WinDivertPacketRecv(null, null);
        }
        var bytes = new MemorySegmentByteArray(pktBuf);
        var ipVer = (bytes.get(0) >> 4) & 0b1111;
        AbstractIpPacket pkt;
        if (ipVer == 4) {
            pkt = new Ipv4Packet();
        } else if (ipVer == 6) {
            pkt = new Ipv6Packet();
        } else {
            assert Logger.lowLevelDebug(STR."unable to parse bytes received from \{this}, bytes=\{bytes.toHexString()}");
            return null;
        }
        var err = pkt.from(new PacketDataBuffer(bytes));
        if (err != null) {
            assert Logger.lowLevelDebug(STR."unable to parse bytes received from \{this}, err = \{err}, bytes=\{bytes.toHexString()}");
            return new WinDivertPacketRecv(null, pktBuf);
        }
        return new WinDivertPacketRecv(pkt, pktBuf);
    }

    public void send(MemorySegment packet, WinDivertRcvSndCtx ctx) throws WinDivertException {
        assert Logger.lowLevelDebug(STR."sending packet to \{handle}, data=\{new MemorySegmentByteArray(packet).toHexString()}, addr=\{ctx.addr}");
        var ok = io.vproxy.windivert.pni.WinDivert.get().send(handle, packet, (int) packet.byteSize(), null, ctx.addr);
        if (!ok) {
            throw new WinDivertException(STR."failed to send packet to \{this}");
        }
    }

    public void send(AbstractIpPacket pkt, WinDivertRcvSndCtx ctx) throws WinDivertException {
        var raw = pkt.getRawPacket(0);
        if (raw.length() > ctx.buf.byteSize()) {
            Logger.error(LogType.IMPROPER_USE, STR."buf size too small: buf=\{ctx.buf.byteSize()}, pkt=\{raw.length()}, pktDesc=\{pkt.description()}");
            return;
        }

        var sndBuf = ctx.buf.copyFrom(MemorySegment.ofArray(raw.toJavaArray())).reinterpret(raw.length());
        send(sndBuf, ctx);
    }

    @Override
    public String toString() {
        return STR."WinDivert(0x\{Long.toHexString(handle.address())})";
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        if (closed) {
            return;
        }
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
        }
        var ok = io.vproxy.windivert.pni.WinDivert.get().close(handle);
        if (!ok) {
            Logger.error(LogType.SYS_ERROR, STR."failed to close \{this}");
        }
    }
}
