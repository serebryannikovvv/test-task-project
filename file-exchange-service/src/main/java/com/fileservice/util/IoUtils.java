package com.fileservice.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class IoUtils {
    private static final int BUFFER_SIZE = 16384;

    private IoUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static long copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        long total = 0;
        int r;
        while ((r = in.read(buf)) != -1) {
            out.write(buf, 0, r);
            total += r;
        }
        out.flush();
        return total;
    }

    public static String readFileContent(Path metaPath) throws IOException {
        if (!Files.exists(metaPath)) {
            return null;
        }
        byte[] bytes = Files.readAllBytes(metaPath);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Читает ресурс из classpath (например, из src/main/resources) в byte[]
     */
    public static byte[] readResourceAsBytes(String resourcePath) {
        try (InputStream is = IoUtils.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                return null;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            java.util.logging.Logger.getLogger(IoUtils.class.getName())
                    .severe("Ошибка чтения ресурса " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }
}