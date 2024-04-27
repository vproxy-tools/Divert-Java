package io.vproxy.windivert.hostsmanager;

import io.vproxy.base.util.LogType;
import io.vproxy.base.util.Logger;
import io.vproxy.base.util.coll.Tuple;
import io.vproxy.vfd.IP;
import io.vproxy.vfd.IPv4;
import io.vproxy.vfd.IPv6;

import java.util.*;

public class HostsParser {
    private HostsParser() {
    }

    public static HostsData parse(String fileName, String content) {
        var ipMap = new LinkedHashMap<IP, Set<String>>();

        var lines = content.split("\n");
        var L = 0;
        for (var line : lines) {
            ++L;
            if (line.contains("#"))
                line = line.substring(0, line.indexOf("#"));
            if (line.isBlank())
                continue;
            var split = line.split("(\\s+)");
            if (split.length < 2) {
                Logger.warn(LogType.INVALID_EXTERNAL_DATA, STR."invalid config at \{fileName}:\{L}");
                continue;
            }
            var ip = split[0];
            if (!IP.isIpLiteral(ip)) {
                Logger.warn(LogType.INVALID_EXTERNAL_DATA, STR."\{ip} is not a valid ip at \{fileName}:\{L}");
                continue;
            }
            var hosts = new HashSet<String>();
            for (var i = 1; i < split.length; ++i) {
                var h = split[i];
                if (IP.isIpLiteral(h)) {
                    Logger.warn(LogType.INVALID_EXTERNAL_DATA, STR."expecting hostname, but \{h} is an ip literal at \{fileName}:\{L}");
                    continue;
                }
                if (!h.endsWith(".")) {
                    h += ".";
                }
                hosts.add(h);
            }
            if (hosts.isEmpty()) {
                continue;
            }
            ipMap.put(IP.from(ip), hosts);
        }

        var hostsMap = new HashMap<String, Tuple<Set<IPv4>, Set<IPv6>>>();
        for (var entry : ipMap.entrySet()) {
            var ip = entry.getKey();
            var hosts = entry.getValue();
            for (var h : hosts) {
                if (!hostsMap.containsKey(h)) {
                    hostsMap.put(h, new Tuple<>(new HashSet<>(), new HashSet<>()));
                }
                var tup = hostsMap.get(h);
                if (ip instanceof IPv4 v4) {
                    tup._1.add(v4);
                } else if (ip instanceof IPv6 v6) {
                    tup._2.add(v6);
                } else {
                    Logger.shouldNotHappen(STR."\{ip} should be IPv4 or IPv6");
                }
            }
        }
        return new HostsData(hostsMap);
    }
}
