package io.vproxy.windivert;

import io.vproxy.base.util.Logger;
import io.vproxy.pni.Allocator;
import io.vproxy.pni.array.LongArray;

public class ProfileAPI {
    private static volatile boolean kernel32Loaded = false;

    private ProfileAPI() {
    }

    private static void loadKernel32() {
        if (kernel32Loaded) {
            return;
        }
        synchronized (ProfileAPI.class) {
            if (kernel32Loaded) {
                return;
            }
            try {
                System.loadLibrary("Kernel32");
            } catch (UnsatisfiedLinkError e) {
                Logger.shouldNotHappen("failed loading library: Kernel32");
                throw e;
            }
            kernel32Loaded = true;
        }
    }

    private static long performanceFrequency = 0;

    public static long getPerformanceFrequency() {
        loadKernel32();
        if (performanceFrequency > 0) {
            return performanceFrequency;
        }
        try (var allocator = Allocator.ofConfined()) {
            var freq = new LongArray(allocator, 1);
            var ok = io.vproxy.windivert.pni.ProfileAPI.get().queryPerformanceFrequency(freq);
            if (!ok) {
                return 0;
            }
            performanceFrequency = freq.get(0);
        }
        return performanceFrequency;
    }
}
