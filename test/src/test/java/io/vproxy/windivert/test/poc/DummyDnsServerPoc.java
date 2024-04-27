package io.vproxy.windivert.test.poc;

import io.vproxy.base.util.LogType;
import io.vproxy.base.util.Logger;
import io.vproxy.pni.Allocator;
import io.vproxy.vfd.IPv4;
import io.vproxy.vpacket.*;
import io.vproxy.vpacket.dns.*;
import io.vproxy.vpacket.dns.rdata.A;
import io.vproxy.windivert.WinDivert;
import io.vproxy.windivert.WinDivertRcvSndCtx;

import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.Scanner;

public class DummyDnsServerPoc {
    private static final String HOST = "divert-test.special.vproxy.io.";
    private static final IPv4 IP = io.vproxy.vfd.IP.fromIPv4("127.1.2.3");

    public static void main(String[] args) throws Exception {
        var divert = WinDivert.open("outbound && udp.DstPort == 53");

        new Thread(() -> handle(divert)).start();

        Logger.alert("Press Enter to exit ...");
        var scanner = new Scanner(System.in);
        scanner.nextLine();
        divert.close();

        WinDivert.unload();
    }

    private static void handle(WinDivert divert) {
        try (var allocator = Allocator.ofConfined()) {
            var ctx = new WinDivertRcvSndCtx(allocator);
            int len = 0;

            var exCnt = 0;
            while (true) {
                boolean stolen = false;
                try {
                    var tup = divert.receive(ctx);
                    if (tup.packet() == null) {
                        if (divert.isClosed()) {
                            break;
                        }
                        Logger.warn(LogType.ALERT, "received nothing ...");
                        if (++exCnt >= 3) {
                            break;
                        }
                        continue;
                    }
                    var pkt = tup.packet();
                    len = (int) tup.raw().byteSize();
                    stolen = handleOne(pkt, divert, ctx);
                    exCnt = 0;
                } catch (Exception e) {
                    if (divert.isClosed()) {
                        break;
                    }
                    e.printStackTrace(System.out);
                    if (++exCnt >= 3) {
                        break;
                    }
                }
                if (stolen) {
                    continue;
                }
                try {
                    sendBack(divert, ctx.buf.reinterpret(len), ctx);
                } catch (Exception e) {
                    if (divert.isClosed()) {
                        break;
                    }
                    e.printStackTrace(System.out);
                }
            }
        }
    }

    private static boolean handleOne(AbstractIpPacket pkt, WinDivert divert, WinDivertRcvSndCtx ctx) throws Exception {
        Logger.alert(STR."found packet: \{pkt.description()}");
        Logger.alert(STR."windivert addr: \{ctx.addr}");

        if (!ctx.addr.isOutbound()) {
            Logger.error(LogType.INVALID_EXTERNAL_DATA, "the packet is inbound");
            return false;
        }
        if (!(pkt.getPacket() instanceof UdpPacket udp)) {
            Logger.error(LogType.INVALID_EXTERNAL_DATA, "upper layer packet is not udp");
            return false;
        }
        if (udp.getDstPort() != 53) {
            Logger.error(LogType.INVALID_EXTERNAL_DATA, "dst port is not 53");
            return false;
        }
        var data = udp.getData().getRawPacket(0);
        List<DNSPacket> dnsPackets;
        try {
            dnsPackets = Formatter.parsePackets(data);
        } catch (Exception e) {
            Logger.warn(LogType.INVALID_EXTERNAL_DATA, "not dns packet", e);
            return false;
        }
        if (dnsPackets.isEmpty()) {
            Logger.warn(LogType.ALERT, "no dns packet found");
            return false;
        }
        if (dnsPackets.size() != 1) {
            Logger.warn(LogType.ALERT, STR."more than one dns packet: \{dnsPackets.size()}");
            return false;
        }
        var dnsPkt = dnsPackets.getFirst();
        Logger.alert(STR."found dns packet: \{dnsPkt}");

        if (dnsPkt.isResponse) {
            Logger.warn(LogType.ALERT, "is dns response");
            return false;
        }
        if (dnsPkt.opcode != DNSPacket.Opcode.QUERY) {
            assert Logger.lowLevelDebug("not QUERY");
            return false;
        }
        if (dnsPkt.questions.isEmpty()) {
            Logger.warn(LogType.ALERT, "questions section is empty");
            return false;
        }
        if (dnsPkt.questions.size() != 1) {
            Logger.warn(LogType.ALERT, "more than one queries");
            return false;
        }
        var q = dnsPkt.questions.getFirst();
        Logger.alert(STR."found dns request for \{q.qtype} \{q.qname}");

        if (q.qtype != DNSType.A && q.qtype != DNSType.ANY) {
            assert Logger.lowLevelDebug("not requesting A record nor ANY record");
            return false;
        }
        if (!q.qname.equals(HOST)) {
            assert Logger.lowLevelDebug(STR."not requesting for \{HOST}");
            return false;
        }

        Logger.alert("!!building response!!");

        var dnsResponse = new DNSPacket();
        dnsResponse.id = dnsPkt.id;
        dnsResponse.isResponse = true;
        dnsResponse.opcode = DNSPacket.Opcode.QUERY;
        dnsResponse.aa = false;
        dnsResponse.tc = false;
        dnsResponse.rd = true;
        dnsResponse.ra = true;
        dnsResponse.rcode = DNSPacket.RCode.NoError;
        dnsResponse.questions.add(q);
        var answer = new DNSResource();
        answer.name = q.qname;
        answer.type = DNSType.A;
        answer.clazz = DNSClass.IN;
        answer.ttl = 5;
        var rdataA = new A();
        answer.rdata = rdataA;
        rdataA.address = IP;
        dnsResponse.answers.add(answer);

        var dnsData = Formatter.format(dnsResponse);
        udp.setData(dnsData);
        udp.setDstPort(udp.getSrcPort());
        udp.setSrcPort(53);

        if (pkt instanceof Ipv4Packet v4) {
            var src = v4.getSrc();
            v4.setSrc(v4.getDst());
            v4.setDst(src);
        } else {
            var v6 = (Ipv6Packet) pkt;
            var src = v6.getSrc();
            v6.setSrc(v6.getDst());
            v6.setDst(src);
        }

        ctx.addr.setOutbound(false);

        divert.send(pkt, ctx);
        return true;
    }

    private static void sendBack(WinDivert divert, MemorySegment buf, WinDivertRcvSndCtx ctx) throws Exception {
        divert.send(buf, ctx);
    }
}
