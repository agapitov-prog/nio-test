package com.becut.cb.alg;

import java.nio.ByteBuffer;

public interface BufferCreator {

    public boolean isHeap();
    
    public ByteBuffer createBuffer(int bufferSize);

}
