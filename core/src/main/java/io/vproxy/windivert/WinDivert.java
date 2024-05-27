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
import java.io.UnsupportedEncodingException;
import java.lang.foreign.MemorySegment;
import java.util.HashSet;
import java.util.Set;

public class WinDivert {
    private static final String DRIVER_NAME = "WinDivert";
    private static final Set<String> ALTERNATIVE_DRIVER_NAMES = Set.of(
        "vproxy_windivert",
        "WinDivert1.0"
    );
    private static final Set<String> OLD_DRIVERS = Set.of("vproxy_windivert");
    private static volatile boolean isLoadedBySelf = false;
    private static volatile boolean isLoadedByOthers = false;
    private static volatile boolean isDllLoaded = false;

    public static boolean isLoaded() {
        if (isLoadedBySelf || isLoadedByOthers) {
            isLoadedBySelf = isLoadedByOthers = openTest();
        }
        return isLoadedBySelf || isLoadedByOthers;
    }

    private static boolean openTest() {
        var wasDLLLoaded = isDllLoaded;
        loadDLL();
        var divert = doOpen("outbound && udp.DstPort == 65535 && udp.DstPort == 65534",
            WinDivertLayer.WINDIVERT_LAYER_NETWORK, 0, 0);
        if (divert == null) {
            if (!wasDLLLoaded) {
                // first time using divert
                // the service would be automatically created by windivert
                // which will be invalid in most cases since we release the .sys with a different name
                unloadSysForce();
            }
            return false;
        }
        divert.close();
        return true;
    }

    public static void load() throws UnsatisfiedLinkError {
        if (isLoaded()) {
            return;
        }
        unloadOldSys();
        if (openTest()) {
            isLoadedByOthers = true;
            return;
        }
        synchronized (WinDivert.class) {
            if (isLoaded()) {
                return;
            }
            try {
                loadSys();
            } catch (Throwable t) {
                Logger.warn(LogType.SYS_ERROR, "failed to load driver, trying to unload then reload", t);
                unloadSys();
                loadSys();
            }
            if (!openTest()) {
                Logger.warn(LogType.SYS_ERROR, "the driver seems to be loaded but is not working properly, " +
                                               "trying to unload all possible drivers then reload");
                unloadAllSys();
                loadSys();
                if (!openTest()) {
                    Logger.error(LogType.SYS_ERROR, "the driver is still not working properly, " +
                                                    "will unload all possible drivers");
                    unloadAllSys();
                    isLoadedBySelf = isLoadedByOthers = false; // ensure the states cleared
                    throw new UnsatisfiedLinkError("failed to load driver");
                }
            }
            isLoadedBySelf = true;
        }
    }

    public static void unload() {
        if (!isLoadedBySelf) {
            return;
        }
        synchronized (WinDivert.class) {
            if (!isLoadedBySelf) {
                return;
            }
            var ok = unloadSys();
            if (ok) {
                isLoadedBySelf = false;
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
        boolean isSysLoaded;
        try {
            isSysLoaded = isSysLoaded();
        } catch (Throwable t) {
            return false;
        }
        if (!isSysLoaded) {
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
        file = STR."binpath=\{file}";
        try {
            var bytes = file.getBytes("GBK");
            file = new String(bytes, "GBK");
        } catch (UnsupportedEncodingException ignore) {
            // ignore error if GBK is not supported
        }
        try {
            var pb = new ProcessBuilder("sc.exe", "create", DRIVER_NAME,
                file, "type=kernel", "start=demand", "error=normal");
            Logger.alert(STR."trying to execute command: \{pb.command()}");
            var res = Utils.execute(pb, 2_000, true);
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

    private static void unloadSysForce() {
        unloadSysForce(DRIVER_NAME);
    }

    private static void unloadSysForce(String driverName) {
        try {
            // ignore output and errors
            Utils.execute(STR."sc.exe stop \{driverName}", true);
            Utils.execute(STR."sc.exe delete \{driverName}", true);
        } catch (Throwable ignore) {
        }
    }

    private static void unloadAllSys() {
        var all = new HashSet<String>();
        all.add(DRIVER_NAME);
        all.addAll(ALTERNATIVE_DRIVER_NAMES);
        all.addAll(OLD_DRIVERS);
        for (var n : all) {
            unloadSysForce(n);
        }
    }

    private static void unloadOldSys() {
        for (var old : OLD_DRIVERS) {
            unloadSysForce(old);
        }
    }

    private static void loadDLL() {
        if (isDllLoaded) {
            return;
        }
        synchronized (WinDivert.class) {
            if (isDllLoaded) {
                return;
            }
            Utils.loadDynamicLibrary("WinDivert", WinDivert.class.getClassLoader(), "io/vproxy/windivert/");
            isDllLoaded = true;
        }
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
        var divert = doOpen(filter, layer, priority, flags);
        if (divert == null) {
            throw new WinDivertException("failed to open WinDivert");
        }
        return divert;
    }

    private static WinDivert doOpen(String filter, WinDivertLayer layer, int priority, long flags) {
        MemorySegment handle;
        try (var allocator = Allocator.ofConfined()) {
            var filterNative = new PNIString(allocator, filter);
            handle = io.vproxy.windivert.pni.WinDivert.get().open(filterNative, layer.value, (short) priority, flags);
            if (handle == null || handle.address() == -1) {
                return null;
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
        if (ctx.recordedTimestampTicks == 0) {
            ctx.recordedTimestampTicks = ctx.addr.getTimestamp();
        }
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
