package io.vproxy.windivert.pni;

import io.vproxy.pni.*;
import io.vproxy.pni.array.*;
import java.lang.foreign.*;
import java.lang.invoke.*;
import java.nio.ByteBuffer;

public class ProfileAPI {
    private ProfileAPI() {
    }

    private static final ProfileAPI INSTANCE = new ProfileAPI();

    public static ProfileAPI get() {
        return INSTANCE;
    }

    private static final MethodHandle queryPerformanceFrequencyMH = PanamaUtils.lookupPNICriticalFunction(new PNILinkOptions().setCritical(true), boolean.class, "QueryPerformanceFrequency", MemorySegment.class /* lpFrequency */);

    public boolean queryPerformanceFrequency(LongArray lpFrequency) {
        boolean RESULT;
        try {
            RESULT = (boolean) queryPerformanceFrequencyMH.invokeExact((MemorySegment) (lpFrequency == null ? MemorySegment.NULL : lpFrequency.MEMORY));
        } catch (Throwable THROWABLE) {
            throw PanamaUtils.convertInvokeExactException(THROWABLE);
        }
        return RESULT;
    }
}
// metadata.generator-version: pni 21.0.0.18
// sha256:00b52d604a6834a7a9b59e1c865ddc84d4157362f1d5f9bcc23fe2da7c20a4bc
