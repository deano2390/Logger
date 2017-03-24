package uk.co.deanwild.logger;

import android.util.Log;

/**
 * Created by deanwild on 27/05/16.
 */
public class Logger {

    static boolean init = false;
    private static boolean shouldLog;

    public static synchronized void init(boolean enableLogging) {
        shouldLog = enableLogging;
        init = true;
    }

    public static void log(String tag, String log) {

        if (!init)
            throw new RuntimeException("Logger not initialized. Call Logger.init() before trying to log");

        if (shouldLog) {
            Log.d(tag, log);
        }
    }

    public static void printStack(String tag) {

        if (!init)
            throw new RuntimeException("Logger not initialized. Call Logger.init() before trying to log");

        if (shouldLog) {
            StackTraceElement[] cause = Thread.currentThread().getStackTrace();
            for (StackTraceElement ste : cause) {
                log(tag, ste.toString());
            }
        }
    }
}
