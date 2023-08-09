package com.becut.cb.alg;

import com.becut.cb.nio.Task;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SimpleReader implements Task {

    private final BufferCreator creator;
    private final int bufferSize;

    private ByteBuffer bb;

    private volatile long readed = 0;

    public SimpleReader(BufferCreator creator, int bufferSize) {
        this.creator = creator;
        this.bufferSize = bufferSize;
    }

    @Override
    public long getProcessedBytes() {
        return readed;
    }

    @Override
    public boolean execute(SocketChannel channel) throws IOException {
        int read;
        for (;;) {
            if (bb != null && bb.hasRemaining()) {
                read = channel.read(bb);
                if (read == 0) {
                    return false;
                } else if (read < 0) {
                    throw new IOException("Closed channel");
                }
                readed += read;
            } else {
                bb = creator.createBuffer(bufferSize);
            }
        }
    }

}
