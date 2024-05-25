package io.vproxy.windivert.pni;

import io.vproxy.pni.annotation.*;

@Downcall
@Include("profileapi.h")
public interface ProfileAPI {
    @Name("QueryPerformanceFrequency")
    @Style(Styles.critical)
    @LinkerOption.Critical
    boolean queryPerformanceFrequency(@Raw long[] lpFrequency);
}
