package com.fileservice.io;

import com.fileservice.exception.SizeLimitExceededException;

import java.io.IOException;
import java.io.InputStream;

public class LimitedInputStream extends InputStream {
    private final InputStream delegate;
    private final long max;
    private long read = 0;

    public LimitedInputStream(InputStream delegate, long max) {
        this.delegate = delegate;
        this.max = max;
    }

    @Override
    public int read() throws IOException {
        int b = delegate.read();
        if (b != -1) check(++read);
        return b;
    }

    @Override
    public int read(byte[] byteArray, int off, int len) throws IOException {
        int n = delegate.read(byteArray, off, len);
        if (n > 0) check(read += n);
        return n;
    }

    @Override
    public long skip(long num) throws IOException {
        long skipped = delegate.skip(num);
        if (skipped > 0) check(read += skipped);
        return skipped;
    }

    private void check(long current) throws SizeLimitExceededException {
        if (current > max) throw new SizeLimitExceededException();
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}

