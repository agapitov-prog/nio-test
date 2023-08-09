package com.becut.cb.nio;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Worker implements Runnable {

    private final NioWorker worker;

    private Thread thread;
    
    private volatile boolean interrupted;

    public Worker(NioWorker worker) {
        this.worker = worker;
        thread = null;
        interrupted = true;
    }

    public synchronized void start() {
        if (thread == null) {
            interrupted = false;
            thread = new Thread(this);
            thread.start();
        }
    }

    public synchronized void stop() {
        interrupted = true;
        while (thread.isAlive()) {
            try {
                thread.join(500);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        thread = null;
    }

    @Override
    public void run() {
        while (!interrupted) {
            try {
                worker.doWork();
            } catch (IOException ex) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
