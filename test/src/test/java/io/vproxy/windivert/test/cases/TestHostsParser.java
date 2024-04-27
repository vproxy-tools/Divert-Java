package io.vproxy.windivert.test.cases;

import io.vproxy.base.util.coll.Tuple;
import io.vproxy.vfd.IP;
import io.vproxy.windivert.hostsmanager.HostsParser;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class TestHostsParser {
    @Test
    public void simple() {
        var data = HostsParser.parse("hosts",
            """
                127.0.0.1 localhost mycomputer
                """);
        assertEquals(
            Map.of(
                "localhost.", new Tuple<>(
                    Set.of(IP.from("127.0.0.1")),
                    Set.of()
                ),
                "mycomputer.", new Tuple<>(
                    Set.of(IP.from("127.0.0.1")),
                    Set.of()
                )),
            data.storage
        );
    }

    @Test
    public void multiline() {
        var data = HostsParser.parse("hosts",
            """
                1.2.3.4 hello.com
                3.4.5.6 goodbye.org
                """);
        assertEquals(
            Map.of(
                "hello.com.", new Tuple<>(
                    Set.of(IP.from("1.2.3.4")),
                    Set.of()
                ),
                "goodbye.org.", new Tuple<>(
                    Set.of(IP.from("3.4.5.6")),
                    Set.of()
                )),
            data.storage
        );
    }

    @Test
    public void ipv6() {
        var data = HostsParser.parse("hosts",
            """
                1.2.3.4 hello.com
                2001:abcd::3e:21 hello.com
                """);
        assertEquals(
            Map.of(
                "hello.com.", new Tuple<>(
                    Set.of(IP.from("1.2.3.4")),
                    Set.of(IP.from("2001:abcd::3e:21"))
                )
            ),
            data.storage
        );
    }

    @Test
    public void comment() {
        var data = HostsParser.parse("/etc/hosts",
            """
                # 127.0.0.1\t\tlocalhost
                fd00::1\t\tabc.com
                """);
        assertEquals(
            Map.of(
                "abc.com.", new Tuple<>(
                    Set.of(),
                    Set.of(IP.from("fd00::1"))
                )
            ),
            data.storage
        );
    }
}
