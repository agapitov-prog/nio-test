package com.becut.cb.alg;

import java.nio.ByteBuffer;

public class BufferFactory {

    public static final BufferCreator HEAP_CREATOR = new BufferCreator() {
        @Override
        public ByteBuffer createBuffer(int bufferSize) {
            return ByteBuffer.allocate(bufferSize);
        }

    };

    public static final BufferCreator DIRECT_CREATOR = new BufferCreator() {
        @Override
        public ByteBuffer createBuffer(int bufferSize) {
            return ByteBuffer.allocateDirect(bufferSize);
        }

    };

}
