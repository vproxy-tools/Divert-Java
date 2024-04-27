package io.vproxy.windivert.hostsmanager;

import io.vproxy.base.util.coll.Tuple;
import io.vproxy.vfd.IP;
import io.vproxy.vfd.IPv4;
import io.vproxy.vfd.IPv6;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HostsStorage {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<Object, HostsData> storage = new HashMap<>();

    public HostsStorage() {
    }

    public void addOrReplace(Object id, HostsData data) {
        lock.writeLock().lock();
        try {
            storage.put(id, data);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean remove(Object id) {
        lock.writeLock().lock();
        try {
            return storage.remove(id) != null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<IPv4> lookupIPv4(String host) {
        var ls = lookupAllIPv4(host);
        if (ls.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ls.getFirst());
    }

    public Optional<IPv6> lookupIPv6(String host) {
        var ls = lookupAllIPv6(host);
        if (ls.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ls.getFirst());
    }

    public Tuple<Optional<IPv4>, Optional<IPv6>> lookupIP(String host) {
        var ls = lookupAllIP(host);
        if (ls.isEmpty()) {
            return new Tuple<>(Optional.empty(), Optional.empty());
        }
        var v4 = ls.stream().filter(i -> i instanceof IPv4).findFirst().map(i -> (IPv4) i);
        var v6 = ls.stream().filter(i -> i instanceof IPv6).findFirst().map(i -> (IPv6) i);
        return new Tuple<>(v4, v6);
    }

    public List<IPv4> lookupAllIPv4(String host) {
        var res = new HashSet<IPv4>();
        lock.readLock().lock();
        try {
            for (var s : storage.values()) {
                var ls = s.storage.get(host);
                if (ls == null)
                    continue;
                res.addAll(ls._1);
            }
        } finally {
            lock.readLock().unlock();
        }
        var ret = new ArrayList<>(res);
        Collections.shuffle(ret);
        return ret;
    }

    public List<IPv6> lookupAllIPv6(String host) {
        var res = new HashSet<IPv6>();
        lock.readLock().lock();
        try {
            for (var s : storage.values()) {
                var ls = s.storage.get(host);
                if (ls == null)
                    continue;
                res.addAll(ls._2);
            }
        } finally {
            lock.readLock().unlock();
        }
        var ret = new ArrayList<>(res);
        Collections.shuffle(ret);
        return ret;
    }

    public List<IP> lookupAllIP(String host) {
        var res = new HashSet<IP>();
        lock.readLock().lock();
        try {
            for (var s : storage.values()) {
                var ls = s.storage.get(host);
                res.addAll(ls._1);
                res.addAll(ls._2);
            }
        } finally {
            lock.readLock().unlock();
        }
        var ret = new ArrayList<>(res);
        Collections.shuffle(ret);
        return ret;
    }
}
