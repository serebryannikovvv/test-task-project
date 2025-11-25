package com.fileservice.exception;

import java.io.IOException;

public class SizeLimitExceededException extends IOException {
    public SizeLimitExceededException() {
        super("Size limit exceeded");
    }
}
