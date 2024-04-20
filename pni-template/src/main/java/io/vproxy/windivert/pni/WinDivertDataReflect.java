package io.vproxy.windivert.pni;

import io.vproxy.pni.annotation.Struct;
import io.vproxy.pni.annotation.Unsigned;

@Struct
public class WinDivertDataReflect {
    long timestamp;
    @Unsigned int processId;
    int layer;
    @Unsigned long flags;
    short priority;
}
