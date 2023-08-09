package com.becut.cb;

import com.becut.cb.alg.BufferFactory;
import com.becut.cb.alg.CachedReader;
import com.becut.cb.alg.CachedWriter;
import com.becut.cb.nio.ClientConnection;
import com.becut.cb.nio.Worker;
import com.becut.cb.nio.ServerConnection;
import com.becut.cb.nio.Server;
import com.becut.cb.nio.Task;
import com.becut.cb.alg.SimpleReader;
import com.becut.cb.alg.SimpleWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.LockSupport;

public class NioTest implements AutoCloseable {

    private static final SocketAddress SOCKET_ADDRESS = new InetSocketAddress("127.0.0.1", 9876);

    private volatile ClientConnection clientConnection;
    private volatile ServerConnection serverConnection;

    private volatile Worker clientWorker, serverWorker;

    private volatile IOException ioex;

    @Override
    public void close() throws IOException {
        if (clientConnection != null) {
            clientConnection.close();
            clientConnection = null;
        }
        if (serverConnection != null) {
            serverConnection.close();
            serverConnection = null;
        }
    }

    private void connectToSelf() throws IOException {

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(SOCKET_ADDRESS);
        ssc.configureBlocking(false);
        Server server = new Server(ssc);
        try {
            server.setImpl(new Task() {
                @Override
                public boolean execute(SocketChannel channel) {
                    try {
                        serverConnection = new ServerConnection(channel);
                        return true;
                    } catch (IOException ex) {
                        ioex = ex;
                    }
                    return false;
                }

                @Override
                public long getProcessedBytes() {
                    return 0;
                }

            });

            clientConnection = new ClientConnection(SocketChannel.open());
            clientConnection.connect(SOCKET_ADDRESS);

            clientWorker = new Worker(clientConnection);
            serverWorker = new Worker(server);

            startWorkers();

            while ((serverConnection == null || !clientConnection.isConnected()) && ioex == null) {
                LockSupport.parkUntil(System.currentTimeMillis() + 500);
            }

            stopWorkers();
            serverWorker = new Worker(serverConnection);

        } finally {
            server.close();
        }

        if (ioex != null) {
            throw ioex;
        }

    }

    private void startWorkers() {
        clientWorker.start();
        serverWorker.start();
    }

    private void stopWorkers() {
        serverWorker.stop();
        clientWorker.stop();
    }

    private String getMegabits(long processedBytes, long timeout) {
        return String.valueOf((processedBytes / 1024 / 1024 / (timeout / 1000)) * 8) + " MBit";
    }

    private long testAlg(int bufferSize, Task client, Task server, long timeout) {
        clientConnection.setImpl(client);
        serverConnection.setImpl(server);
        try {
            clientConnection.cont();
            serverConnection.cont();
            startWorkers();
            LockSupport.parkUntil(System.currentTimeMillis() + timeout);
            stopWorkers();
            LockSupport.parkUntil(System.currentTimeMillis() + 5000);
            return server.getProcessedBytes();
        } finally {
            clientConnection.freeze();
            serverConnection.freeze();
        }
    }

    public static void main(String[] args) throws IOException {

        int[] bufferSizes = new int[]{
            1024, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576
        };
        int timeToTest = 60000;

        try (NioTest test = new NioTest()) {

            test.connectToSelf();

            System.out.println("\tSimple heap\tSimple direct\tCached heap\tCached direct\n");
            long processed;

            for (int bufferSize : bufferSizes) {
                
                System.out.print(String.valueOf(bufferSize / 1024) + "Kb");

                processed = test.testAlg(bufferSize, new SimpleWriter(BufferFactory.HEAP_CREATOR, bufferSize), new SimpleReader(BufferFactory.HEAP_CREATOR, bufferSize), timeToTest);
                System.out.print("\t" + test.getMegabits(processed, timeToTest));

                processed = test.testAlg(bufferSize, new SimpleWriter(BufferFactory.DIRECT_CREATOR, bufferSize), new SimpleReader(BufferFactory.DIRECT_CREATOR, bufferSize), timeToTest);
                System.out.print("\t" + test.getMegabits(processed, timeToTest));

                processed = test.testAlg(bufferSize, new CachedWriter(BufferFactory.HEAP_CREATOR, bufferSize), new CachedReader(BufferFactory.HEAP_CREATOR, bufferSize), timeToTest);
                System.out.print("\t" + test.getMegabits(processed, timeToTest));

                processed = test.testAlg(bufferSize, new CachedWriter(BufferFactory.DIRECT_CREATOR, bufferSize), new CachedReader(BufferFactory.DIRECT_CREATOR, bufferSize), timeToTest);
                System.out.print("\t" + test.getMegabits(processed, timeToTest));

                System.out.print("\n");
            }

        }

        System.exit(0);
    }

}
