package io.vproxy.windivert.pni;

import io.vproxy.pni.annotation.Struct;
import io.vproxy.pni.annotation.Unsigned;

@Struct
public class WinDivertDataNetwork {
    @Unsigned int ifIdx;
    @Unsigned int subIfIdx;
}
