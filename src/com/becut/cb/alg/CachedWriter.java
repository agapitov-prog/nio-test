package com.becut.cb.alg;

import com.becut.cb.nio.Task;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;

public class CachedWriter implements Task {

    private final ByteBuffer bb;

    public CachedWriter(BufferCreator creator, int bufferSize) {
        bb = creator.createBuffer(bufferSize);
        byte[] data = new byte[bufferSize];
        new Random().nextBytes(data);
        bb.put(data);
        bb.position(0);
    }

    @Override
    public long getProcessedBytes() {
        return 0;
    }

    @Override
    public boolean execute(SocketChannel channel) throws IOException {
        for (;;) {
            if (bb.hasRemaining()) {
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
                bb.position(0);
            }
        }

    }

}
