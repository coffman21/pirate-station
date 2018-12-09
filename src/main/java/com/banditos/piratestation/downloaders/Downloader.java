package com.banditos.piratestation.downloaders;

import java.io.IOException;

public interface Downloader {
    byte[] download(String query) throws IOException;

    
}
