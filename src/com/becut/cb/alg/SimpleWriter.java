package com.becut.cb.alg;

import com.becut.cb.nio.Task;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;

public class SimpleWriter implements Task {

    private final BufferCreator creator;
    private final byte[] data;

    private ByteBuffer bb;

    public SimpleWriter(BufferCreator creator, int bufferSize) {
        this.creator = creator;
        data = new byte[bufferSize];
        new Random().nextBytes(data);
    }

    @Override
    public long getProcessedBytes() {
        return 0;
    }

    @Override
    public boolean execute(SocketChannel channel) throws IOException {
        for (;;) {
            if (bb != null && bb.hasRemaining()) {
                int size = channel.write(bb);
                if (size > 0) {
                    continue;
                }
                if (size == 0) {
                    if (bb.hasRemaining()) {
                        return false;
                    }
                } else {
                    throw new IOException("Closed connection");
                }
            } else {
                bb = creator.createBuffer(data.length);
                bb.put(data);
                bb.position(0);
            }
        }
    }

}
