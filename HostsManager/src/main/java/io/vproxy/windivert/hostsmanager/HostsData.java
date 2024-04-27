package io.vproxy.windivert.hostsmanager;

import io.vproxy.base.util.coll.Tuple;
import io.vproxy.vfd.IPv4;
import io.vproxy.vfd.IPv6;

import java.util.Map;
import java.util.Set;

public class HostsData {
    public final Map<String, Tuple<Set<IPv4>, Set<IPv6>>> storage;

    public HostsData(Map<String, Tuple<Set<IPv4>, Set<IPv6>>> storage) {
        this.storage = Map.copyOf(storage);
    }
}
