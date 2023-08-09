package com.becut.cb.nio;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface Task {

    public boolean execute(SocketChannel channel) throws IOException;
    
    public long getProcessedBytes();
    
}
