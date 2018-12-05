package uk.co.deanwild.logger;

import android.content.Context;
import android.os.Process;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import timber.log.Timber;

class CustomFileLoggingTree extends Timber.Tree implements Runnable {

    static long MAX_FILE_SIZE = 100000l; // 100kb
    static long MAX_TOTAL_SIZE = 20000000; // 20mb
    DateFormat simple = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS");

    final Context context;
    final Thread thread;
    final File logDirectory;
    LinkedBlockingQueue<String> inputQueue;
    FileOutputStream outputStream;

    File currentLogFile;

    public CustomFileLoggingTree(Context context, File logDirectory) {
        this.context = context;
        this.logDirectory = logDirectory;
        this.inputQueue = new LinkedBlockingQueue<>();
        thread = new Thread(this);
        thread.start();
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        try {
            inputQueue.put(tag + " : " + message);

            synchronized (inputQueue) {
                inputQueue.notify();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
        try {
            trimFiles();
            startNewFile();
            startLogging();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void startLogging() throws IOException {
        while (!thread.isInterrupted()) {

            String log = inputQueue.poll();
            if (log != null) {
                if (outputStream != null) {
                    String logMessage = simple.format(new Date(System.currentTimeMillis())) + " " + log + "\n";
                    outputStream.write(logMessage.getBytes());
                }
            } else {

                checkSize();

                // sleep thread until wake, avoids un-needed spinning
                synchronized (inputQueue) {
                    try {
                        inputQueue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    void checkSize() throws IOException {

        trimFiles();

        if (currentLogFile != null && currentLogFile.length() > MAX_FILE_SIZE) {
            startNewFile();
        }
    }

    void startNewFile() throws IOException {
        currentLogFile = createRandomLogFile();
        outputStream = new FileOutputStream(currentLogFile);
    }

    File createRandomLogFile() throws IOException {
        File file = new File(logDirectory.getAbsolutePath() + "/" + UUID.randomUUID() + ".txt");
        if (!file.exists()) {
            file.createNewFile();
            return file;
        } else {
            return createRandomLogFile();
        }
    }

    void trimFiles() {
        long targetSize = (long) (MAX_TOTAL_SIZE * 0.75);
        long totalSize = getTotalFolderSize();

        while (totalSize > targetSize) {
            File file = findYoungestFile();
            totalSize -= file.length();
            file.delete();
        }
    }

    private File findYoungestFile() {
        File[] files = logDirectory.listFiles();
        if (files != null && files.length > 0) {
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
            return files[0];
        }
        return null;
    }

    long getTotalFolderSize() {
        long total = 0;
        File[] files = logDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                total += file.length();
            }
        }
        return total;
    }
}
