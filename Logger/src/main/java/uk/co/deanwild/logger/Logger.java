package uk.co.deanwild.logger;

import android.content.Context;

import java.io.File;

import timber.log.Timber;

/**
 * Created by deanwild on 27/05/16.
 */
public class Logger {

    static Logger instance;

    public static synchronized Logger getInstance() {

        if (instance == null) {
            instance = new Logger();
        }

        return instance;
    }

    boolean enableConsoleLogging;
    boolean enableDiskLogging;
    File logDirectory;

    public void enableConsoleLogging() {
        this.enableConsoleLogging = true;
        Timber.plant(new Timber.DebugTree());
    }

    public void enableDiskLogging(Context context, File logDirectory) {
        this.enableDiskLogging = true;
        this.logDirectory = logDirectory;
        Timber.plant(new CustomFileLoggingTree(context, logDirectory));
    }


    public static void log(String tag, String log) {
        getInstance().logInternal(tag, log);
    }

    void logInternal(String tag, String log) {
        Timber.tag(tag);
        Timber.d(log);
    }

    public static void printStack(String tag) {
        getInstance().printStackInternal(tag);
    }

    void printStackInternal(String tag) {
        if (enableConsoleLogging) {
            StackTraceElement[] cause = Thread.currentThread().getStackTrace();
            for (StackTraceElement ste : cause) {
                logInternal(tag, ste.toString());
            }
        }
    }

    public File getLogDirectory() {
        return logDirectory;
    }
}
