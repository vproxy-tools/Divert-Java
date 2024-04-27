package io.vproxy.windivert.hostsmanager;

import io.vproxy.base.util.ByteArray;
import io.vproxy.base.util.LogType;
import io.vproxy.base.util.Logger;
import io.vproxy.base.util.Utils;
import io.vproxy.base.util.coll.Tuple;
import io.vproxy.vfd.IPv4;
import io.vproxy.vfd.IPv6;
import io.vproxy.vpacket.*;
import io.vproxy.vpacket.dns.*;
import io.vproxy.vpacket.dns.rdata.A;
import io.vproxy.vpacket.dns.rdata.AAAA;
import io.vproxy.windivert.WinDivert;
import io.vproxy.windivert.WinDivertException;
import io.vproxy.windivert.WinDivertRcvSndCtx;

import java.util.ArrayList;
import java.util.List;

public class DNSResponder {
    protected final HostsStorage storage;

    public DNSResponder(HostsStorage storage) {
        this.storage = storage;
    }

    public boolean handle(AbstractIpPacket ip, WinDivert divert, WinDivertRcvSndCtx ctx) throws WinDivertException {
        if (ip == null) {
            return false;
        }
        // ctx is null in unit tests
        if (ctx != null && !ctx.addr.isOutbound()) {
            assert Logger.lowLevelDebug("is not outbound packet");
            return false;
        }
        if (!(ip.getPacket() instanceof UdpPacket udp)) {
            assert Logger.lowLevelDebug("is not udp packet");
            return false;
        }
        if (udp.getDstPort() != 53) {
            assert Logger.lowLevelDebug("dst port is not 53");
            return false;
        }
        var data = udp.getData().getRawPacket(0);
        List<DNSPacket> dnsPackets;
        try {
            dnsPackets = Formatter.parsePackets(data);
        } catch (InvalidDNSPacketException e) {
            assert Logger.lowLevelDebug(STR."is not valid dns packet: \{Utils.formatErr(e)}");
            return false;
        }

        if (dnsPackets.isEmpty()) {
            assert Logger.lowLevelDebug("no dns packets");
            return false;
        }

        var modified = false;
        var inbound = new ArrayList<DNSPacket>();
        var outbound = new ArrayList<DNSPacket>();
        for (var dns : dnsPackets) {
            var res = handle(dns);
            if (res == null) {
                outbound.add(dns);
            } else if (res._2 == HandleResult.OUTBOUND) {
                if (res._1 == null) {
                    outbound.add(dns);
                } else {
                    outbound.add(res._1);
                    modified = true;
                }
            } else if (res._2 == HandleResult.INBOUND) {
                //noinspection ReplaceNullCheck
                if (res._1 == null) {
                    inbound.add(dns);
                } else {
                    inbound.add(res._1);
                }
                modified = true;
            } else if (res._2 == HandleResult.DROP) {
                Logger.warn(LogType.ALERT, STR."dns packet \{dns} will be dropped");
                modified = true; // so it will be dropped later
            }
        }

        if (!modified) {
            assert Logger.lowLevelDebug("nothing handled");
            return false;
        }

        if (!inbound.isEmpty()) {
            var ipX = ip;
            if (!outbound.isEmpty()) {
                ipX = ipX.copy();
            }
            joinAndSend(inbound, ipX, false, divert, ctx);
        }
        if (!outbound.isEmpty()) {
            joinAndSend(outbound, ip, true, divert, ctx);
        }

        return true;
    }

    private enum HandleResult {
        DROP, INBOUND, OUTBOUND,
    }

    private Tuple<DNSPacket, HandleResult> handle(DNSPacket dns) {
        if (dns.isResponse) {
            assert Logger.lowLevelDebug("is response");
            return null;
        }
        if (dns.opcode != DNSPacket.Opcode.QUERY) {
            assert Logger.lowLevelDebug("not QUERY");
            return null;
        }
        if (dns.questions.isEmpty()) {
            assert Logger.lowLevelDebug("empty questions list");
            return null;
        }

        var answers = new ArrayList<DNSResource>();
        var handled = false;
        var needDrop = false; // effective only when handled is true
        for (var q : dns.questions) {
            if (q.qtype != DNSType.A && q.qtype != DNSType.AAAA && q.qtype != DNSType.ANY) {
                assert Logger.lowLevelDebug("not A nor ANY type");
                needDrop = true;
                continue;
            }
            if (q.qclass != DNSClass.IN && q.qclass != DNSClass.ANY) {
                assert Logger.lowLevelDebug("not IN nor ANY class");
                needDrop = true;
                continue;
            }

            Logger.trace(LogType.ALERT, STR."lookup \{q.qname} \{q.qclass}");

            var host = q.qname;
            if (!host.endsWith(".")) {
                host += ".";
            }
            if (q.qtype == DNSType.A) {
                var opt = storage.lookupIPv4(host);
                if (opt.isEmpty()) {
                    assert Logger.lowLevelDebug(STR."\{host} A not found");
                    needDrop = true;
                    continue;
                }
                addV4Answer(answers, q, opt.get());
                handled = true;
            } else if (q.qtype == DNSType.AAAA) {
                var opt = storage.lookupIPv6(host);
                if (opt.isEmpty()) {
                    assert Logger.lowLevelDebug(STR."\{host} AAAA not found");
                    needDrop = true;
                    continue;
                }
                addV6Answer(answers, q, opt.get());
                handled = true;
            } else {
                assert q.qtype == DNSType.ANY;
                var tup = storage.lookupIP(host);
                tup._1.ifPresent(iPv4 -> addV4Answer(answers, q, iPv4));
                tup._2.ifPresent(iPv6 -> addV6Answer(answers, q, iPv6));
                if (tup._1.isEmpty() && tup._2.isEmpty()) {
                    assert Logger.lowLevelDebug(STR."\{host} ANY not found");
                    needDrop = true;
                    continue;
                }
                handled = true;
            }
        }

        if (!handled) {
            assert Logger.lowLevelDebug("nothing handled");
            return null;
        }
        if (needDrop) {
            assert Logger.lowLevelDebug("need to be dropped");
            return new Tuple<>(null, HandleResult.DROP);
        }

        var ret = new DNSPacket();
        ret.id = dns.id;
        ret.isResponse = true;
        ret.opcode = DNSPacket.Opcode.QUERY;
        ret.aa = false;
        ret.tc = false;
        ret.rd = true;
        ret.ra = true;
        ret.rcode = DNSPacket.RCode.NoError;
        ret.questions = dns.questions;
        ret.answers = answers;
        return new Tuple<>(ret, HandleResult.INBOUND);
    }

    private void addV4Answer(ArrayList<DNSResource> answers, DNSQuestion q, IPv4 v4) {
        var res = new DNSResource();

        res.name = q.qname;
        res.type = DNSType.A;
        res.clazz = DNSClass.IN;
        var aaaa = new A();
        res.rdata = aaaa;
        aaaa.address = v4;

        answers.add(res);
        Logger.trace(LogType.ALERT, STR."resolved: \{q.qname} -> \{v4.formatToIPString()}");
    }

    private void addV6Answer(ArrayList<DNSResource> answers, DNSQuestion q, IPv6 v6) {
        var res = new DNSResource();

        res.name = q.qname;
        res.type = DNSType.AAAA;
        res.clazz = DNSClass.IN;
        var aaaa = new AAAA();
        res.rdata = aaaa;
        aaaa.address = v6;

        answers.add(res);
        Logger.trace(LogType.ALERT, STR."resolved: \{q.qname} -> \{v6.formatToIPString()}");
    }

    private void joinAndSend(ArrayList<DNSPacket> packets, AbstractIpPacket ip, boolean outbound,
                             WinDivert divert, WinDivertRcvSndCtx ctx) throws WinDivertException {
        if (!outbound) {
            if (ip instanceof Ipv4Packet v4) {
                var i = v4.getSrc();
                v4.setSrc(v4.getDst());
                v4.setDst(i);
            } else {
                var v6 = (Ipv6Packet) ip;
                var i = v6.getSrc();
                v6.setSrc(v6.getDst());
                v6.setDst(i);
            }
            var udp = (UdpPacket) ip.getPacket();
            var p = udp.getSrcPort();
            udp.setSrcPort(udp.getDstPort());
            udp.setDstPort(p);
        }
        ByteArray data = null;
        assert packets != null;
        for (var dns : packets) {
            var array = dns.toByteArray();
            if (data == null) {
                data = array;
            } else {
                data = data.concat(array);
            }
        }
        assert data != null;

        var udp = (UdpPacket) ip.getPacket();
        udp.setData(data);

        // ctx is null in unit tests
        if (ctx != null) {
            ctx.addr.setOutbound(outbound);
        }
        send(ip, divert, ctx);
    }

    protected void send(AbstractIpPacket ip, WinDivert divert, WinDivertRcvSndCtx ctx) throws WinDivertException {
        divert.send(ip, ctx);
    }
}
