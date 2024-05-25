# Divert-Java

An out-of-the-box Java wrapper for WinDivert, using Project Panama FFI.

## How to use

### JDK

You will need at least JDK 21.

### Dependency

#### core

```groovy
implementation 'io.vproxy:divert-java-core:1.1.0'
```

```xml
<dependency>
  <groupId>io.vproxy</groupId>
  <artifactId>divert-java-core</artifactId>
  <version>1.1.0</version>
</dependency>
```

#### HostsManager

```groovy
implementation 'io.vproxy:divert-java-hostsmanager:1.0.2'
```

```xml
<dependency>
  <groupId>io.vproxy</groupId>
  <artifactId>divert-java-hostsmanager</artifactId>
  <version>1.0.2</version>
</dependency>
```

### Coding

```java
import io.vproxy.windivert.*;
import io.vproxy.pni.Allocator;

// load driver and DLL, then open WinDivert handle
var divert = Divert.open("outbound && udp.DstPort == 53");

// receiving and sending thread
new Thread(() -> {
    try (var allocator = Allocator.ofConfined()) {
        var ctx = new WinDivertRcvSndCtx(allocator);

        // receiving ...
        var segment = divert.receiveRaw(ctx);
        if (segment == null) { // received len==0 packet?
            return;
        }
        // ... or ...
        var tup = divert.receive(ctx); // tup is non-null
        if (tup.packet() == null) { // received unrecognized packet?
            return;
        }
        var pkt = tup.packet(); // see io.vproxy.vpacket.*
        segment = tup.raw();

        // sending ...
        divert.send(segment, ctx);
        // ... or ...
        divert.send(pkt, ctx);
    } catch (WinDivertException e) {
        // ...
    }
}).start();

// ... at the end of the program
divert.close();
// and optionally ...
Divert.unload();
```

## Sample

Run with administrator permission:

```
.\gradlew.bat -DPoc=DummyDnsServerPoc clean runPoc
```

Hit `Enter` to exit.

Then in another terminal:

```
ping divert-test.special.vproxy.io
```
