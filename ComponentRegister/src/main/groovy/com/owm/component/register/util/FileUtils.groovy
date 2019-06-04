package com.owm.component.register.util;

/**
 * Created by "ouweiming" on 2019/6/4.
 */
class FileUtils {

    static void close(Closeable... closeable) {
        if (closeable != null) closeable.each { try { it.close() } catch (Exception ignored) {} }
    }

}
