package io.vproxy.windivert.test.cases;

import io.vproxy.base.util.coll.Tuple;
import io.vproxy.pni.Allocator;
import io.vproxy.vfd.IP;
import io.vproxy.vfd.IPPort;
import io.vproxy.vfd.IPv4;
import io.vproxy.vfd.IPv6;
import io.vproxy.vpacket.*;
import io.vproxy.vpacket.dns.*;
import io.vproxy.vpacket.dns.rdata.A;
import io.vproxy.vpacket.dns.rdata.AAAA;
import io.vproxy.windivert.WinDivert;
import io.vproxy.windivert.WinDivertRcvSndCtx;
import io.vproxy.windivert.hostsmanager.DNSResponder;
import io.vproxy.windivert.hostsmanager.HostsData;
import io.vproxy.windivert.hostsmanager.HostsStorage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class TestResponder {
    private interface DNSQueryBuilder {
        void handle(int index, DNSQuestion q);
    }

    private static AbstractIpPacket commonBuildAndTest(IPPort src, IPPort dst, int questionCount, DNSQueryBuilder builder) {
        assert (src.getAddress() instanceof IPv4 && dst.getAddress() instanceof IPv4) || (src.getAddress() instanceof IPv6 && dst.getAddress() instanceof IPv6);
        AbstractIpPacket ipPkt;
        if (src.getAddress() instanceof IPv4) {
            var v4 = new Ipv4Packet();
            ipPkt = v4;
            v4.setSrc((IPv4) src.getAddress());
            v4.setDst((IPv4) dst.getAddress());
        } else {
            var v6 = new Ipv6Packet();
            ipPkt = v6;
            v6.setSrc((IPv6) src.getAddress());
            v6.setDst((IPv6) dst.getAddress());
        }
        var udp = new UdpPacket();
        udp.setSrcPort(src.getPort());
        udp.setDstPort(dst.getPort());
        var dns = buildDNSPacket(questionCount, builder);

        udp.setData(dns.toByteArray());
        ipPkt.setPacket(udp);

        return ipPkt;
    }

    private static DNSPacket buildDNSPacket(int questionCount, DNSQueryBuilder builder) {
        var dns = new DNSPacket();
        dns.id = 0x1234;
        dns.isResponse = false;
        dns.opcode = DNSPacket.Opcode.QUERY;
        dns.aa = false;
        dns.tc = false;
        dns.rd = true;
        dns.ra = false;
        dns.rcode = DNSPacket.RCode.NoError;
        dns.questions = new ArrayList<>(questionCount);
        for (int i = 0; i < questionCount; ++i) {
            var q = new DNSQuestion();
            q.qclass = DNSClass.IN;
            builder.handle(i, q);
            dns.questions.add(q);
        }
        return dns;
    }

    private static List<DNSPacket> commonVerify(AbstractIpPacket ip, IPPort src, IPPort dst) {
        var udp = (UdpPacket) ip.getPacket();
        assertEquals(src.getAddress(), ip.getSrc());
        assertEquals(src.getPort(), udp.getSrcPort());
        assertEquals(dst.getAddress(), ip.getDst());
        assertEquals(dst.getPort(), udp.getDstPort());

        var data = udp.getData().getRawPacket(0);
        List<DNSPacket> packets;
        try {
            packets = Formatter.parsePackets(data);
        } catch (InvalidDNSPacketException e) {
            throw new RuntimeException(e);
        }
        return packets;
    }

    @Test
    public void v4v6() throws Exception {
        var storage = new HostsStorage();
        storage.addOrReplace(1, new HostsData(Map.of(
            "abc.com.", new Tuple<>(
                Set.of(IPv4.fromIPv4("10.0.0.1")),
                Set.of(IPv6.fromIPv6("fd00::1"))
            )
        )));
        var src = new IPPort("10.0.0.2", 12345);
        var dst = new IPPort("10.0.0.1", 53);
        var ipPkt = commonBuildAndTest(src, dst, 2,
            (idx, q) -> {
                if (idx == 0) {
                    q.qtype = DNSType.A;
                } else {
                    q.qtype = DNSType.AAAA;
                }
                q.qname = "abc.com.";
            });
        int[] sendCalled = {0};
        var responder = new DNSResponder(storage) {
            @Override
            protected void send(AbstractIpPacket ip, WinDivert divert, WinDivertRcvSndCtx ctx) {
                ++sendCalled[0];
                var packets = commonVerify(ip, dst, src);

                assertEquals(1, packets.size());
                var dns = packets.getFirst();
                assertEquals(2, dns.answers.size());
                var _1 = dns.answers.get(0);
                var _2 = dns.answers.get(1);
                assertEquals(IP.from("10.0.0.1"), ((A) _1.rdata).address);
                assertEquals(IP.from("fd00::1"), ((AAAA) _2.rdata).address);
            }
        };
        var handled = responder.handle(ipPkt, null, null);
        assertTrue(handled);
        assertEquals(1, sendCalled[0]);
    }

    @Test
    public void dropWhenOneNotResolvedOneResolved() throws Exception {
        var storage = new HostsStorage();
        storage.addOrReplace(1, new HostsData(Map.of(
            "abc.com.", new Tuple<>(
                Set.of(IPv4.fromIPv4("10.0.0.1")), Set.of()
            )
        )));
        var src = new IPPort("10.0.0.2", 12345);
        var dst = new IPPort("10.0.0.1", 53);
        var ipPkt = commonBuildAndTest(src, dst, 4,
            (idx, q) -> {
                if (idx == 0 || idx == 1) {
                    q.qtype = DNSType.A;
                } else {
                    q.qtype = DNSType.AAAA;
                }
                if (idx == 0 || idx == 2) {
                    q.qname = "abc.com.";
                } else {
                    q.qname = "hello.com.";
                }
            });
        int[] sendCalled = {0};
        var responder = new DNSResponder(storage) {
            @Override
            protected void send(AbstractIpPacket ip, WinDivert divert, WinDivertRcvSndCtx ctx) {
                ++sendCalled[0];
            }
        };
        var handled = responder.handle(ipPkt, null, null);
        assertTrue(handled);
        assertEquals(0, sendCalled[0]);
    }

    @Test
    public void resolveAnyV4Only() throws Exception {
        var storage = new HostsStorage();
        storage.addOrReplace(1, new HostsData(Map.of(
            "abc.com.", new Tuple<>(
                Set.of(IPv4.fromIPv4("10.0.0.1")), Set.of()
            )
        )));
        var src = new IPPort("10.0.0.2", 12345);
        var dst = new IPPort("10.0.0.1", 53);
        var ipPkt = commonBuildAndTest(src, dst, 1,
            (_, q) -> {
                q.qtype = DNSType.ANY;
                q.qname = "abc.com.";
            });
        int[] sendCalled = {0};
        var responder = new DNSResponder(storage) {
            @Override
            protected void send(AbstractIpPacket ip, WinDivert divert, WinDivertRcvSndCtx ctx) {
                ++sendCalled[0];
                var packets = commonVerify(ip, dst, src);

                assertEquals(1, packets.size());
                var dns = packets.getFirst();
                assertEquals(1, dns.answers.size());
                var _1 = dns.answers.getFirst();
                assertEquals(IP.from("10.0.0.1"), ((A) _1.rdata).address);
            }
        };
        var handled = responder.handle(ipPkt, null, null);
        assertTrue(handled);
        assertEquals(1, sendCalled[0]);
    }

    @Test
    public void resolveAnyV6Only() throws Exception {
        var storage = new HostsStorage();
        storage.addOrReplace(1, new HostsData(Map.of(
            "abc.com.", new Tuple<>(
                Set.of(), Set.of(IPv6.fromIPv6("fd00::1"))
            )
        )));
        var src = new IPPort("10.0.0.2", 12345);
        var dst = new IPPort("10.0.0.1", 53);
        var ipPkt = commonBuildAndTest(src, dst, 1,
            (_, q) -> {
                q.qtype = DNSType.ANY;
                q.qname = "abc.com.";
            });
        int[] sendCalled = {0};
        var responder = new DNSResponder(storage) {
            @Override
            protected void send(AbstractIpPacket ip, WinDivert divert, WinDivertRcvSndCtx ctx) {
                ++sendCalled[0];
                var packets = commonVerify(ip, dst, src);

                assertEquals(1, packets.size());
                var dns = packets.getFirst();
                assertEquals(1, dns.answers.size());
                var _1 = dns.answers.getFirst();
                assertEquals(IP.from("fd00::1"), ((AAAA) _1.rdata).address);
            }
        };
        var handled = responder.handle(ipPkt, null, null);
        assertTrue(handled);
        assertEquals(1, sendCalled[0]);
    }

    @Test
    public void resolveAnyV4V6() throws Exception {
        var storage = new HostsStorage();
        storage.addOrReplace(1, new HostsData(Map.of(
            "abc.com.", new Tuple<>(
                Set.of(IPv4.fromIPv4("10.0.0.1")),
                Set.of(IPv6.fromIPv6("fd00::1"))
            )
        )));
        var src = new IPPort("10.0.0.2", 12345);
        var dst = new IPPort("10.0.0.1", 53);
        var ipPkt = commonBuildAndTest(src, dst, 1,
            (_, q) -> {
                q.qtype = DNSType.ANY;
                q.qname = "abc.com.";
            });
        int[] sendCalled = {0};
        var responder = new DNSResponder(storage) {
            @Override
            protected void send(AbstractIpPacket ip, WinDivert divert, WinDivertRcvSndCtx ctx) {
                ++sendCalled[0];
                var packets = commonVerify(ip, dst, src);

                assertEquals(1, packets.size());
                var dns = packets.getFirst();
                assertEquals(2, dns.answers.size());
                var _1 = dns.answers.get(0);
                var _2 = dns.answers.get(1);
                assertEquals(IP.from("10.0.0.1"), ((A) _1.rdata).address);
                assertEquals(IP.from("fd00::1"), ((AAAA) _2.rdata).address);
            }
        };
        var handled = responder.handle(ipPkt, null, null);
        assertTrue(handled);
        assertEquals(1, sendCalled[0]);
    }

    @Test
    public void sendBothInboundOutbound() throws Exception {
        var storage = new HostsStorage();
        storage.addOrReplace(1, new HostsData(Map.of(
            "abc.com.", new Tuple<>(
                Set.of(),
                Set.of(IPv6.fromIPv6("fd00::1"))
            )
        )));
        var src = new IPPort("10.0.0.2", 12345);
        var dst = new IPPort("10.0.0.1", 53);
        var ipPkt = commonBuildAndTest(src, dst, 0, (_, _) -> {
        });
        var udp = (UdpPacket) ipPkt.getPacket();
        var dns1 = buildDNSPacket(1, (_, q) -> {
            q.qtype = DNSType.A;
            q.qname = "abc.com.";
        });
        var dns2 = buildDNSPacket(1, (_, q) -> {
            q.qtype = DNSType.AAAA;
            q.qname = "abc.com.";
        });
        udp.setData(dns1.toByteArray().concat(dns2.toByteArray()));

        int[] sendCalled = {0};
        var responder = new DNSResponder(storage) {
            @Override
            protected void send(AbstractIpPacket ip, WinDivert divert, WinDivertRcvSndCtx ctx) {
                ++sendCalled[0];
                List<DNSPacket> packets;
                if (sendCalled[0] == 1) {
                    packets = commonVerify(ip, dst, src);
                    assertFalse(ctx.addr.isOutbound());
                } else {
                    packets = commonVerify(ip, src, dst);
                    assertTrue(ctx.addr.isOutbound());
                }

                assertEquals(1, packets.size());
                var dns = packets.getFirst();
                if (sendCalled[0] == 1) {
                    assertEquals(1, dns.answers.size());
                    var _1 = dns.answers.getFirst();
                    assertEquals(IP.from("fd00::1"), ((AAAA) _1.rdata).address);
                } else {
                    assertEquals(0, dns.answers.size());
                    assertEquals(1, dns.questions.size());
                    var _1 = dns.questions.getFirst();
                    assertEquals("abc.com.", _1.qname);
                    assertEquals(DNSType.A, _1.qtype);
                }
            }
        };
        try (var allocator = Allocator.ofConfined()) {
            var ctx = new WinDivertRcvSndCtx(allocator);
            ctx.addr.setOutbound(true);
            var handled = responder.handle(ipPkt, null, ctx);
            assertTrue(handled);
        }
        assertEquals(2, sendCalled[0]);
    }
}
