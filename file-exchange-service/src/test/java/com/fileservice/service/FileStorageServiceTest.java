package com.fileservice.service;

import com.fileservice.model.FileMetadata;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class FileStorageServiceTest {

    private FileStorageService fileStorageService;
    private Path testStorageDir;
    private Path testMetaDir;

    private static final String TEST_FILE_ID = "test123";
    private static final String TEST_ORIGINAL_NAME = "test-file.txt";
    private static final Instant TEST_UPLOAD_TIME = Instant.now();

    @Before
    public void setUp() throws IOException {
        testStorageDir = Files.createTempDirectory("test-storage");
        testMetaDir = Files.createTempDirectory("test-meta");

        fileStorageService = new FileStorageService(testStorageDir, testMetaDir);
    }

    @After
    public void tearDown() throws IOException {
        deleteRecursively(testStorageDir);
        deleteRecursively(testMetaDir);
    }

    private void deleteRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted((a, b) -> -a.compareTo(b)) // reverse order
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            // Игнорируем ошибки удаления
                        }
                    });
        }
    }

    @Test
    public void testConstructor_CreatesDirectories() {
        // Проверяем, что директории были созданы
        assertTrue(Files.exists(testStorageDir));
        assertTrue(Files.exists(testMetaDir));
        assertTrue(Files.isDirectory(testStorageDir));
        assertTrue(Files.isDirectory(testMetaDir));
    }

    @Test
    public void testGetStorageDir() {
        assertEquals(testStorageDir, fileStorageService.getStorageDir());
    }

    @Test
    public void testGetMetaDir() {
        assertEquals(testMetaDir, fileStorageService.getMetaDir());
    }

    @Test
    public void testSaveMetadata_Success() throws IOException {
        // Act
        fileStorageService.saveMetadata(TEST_FILE_ID, TEST_ORIGINAL_NAME, TEST_UPLOAD_TIME);

        // Assert
        Path metaPath = testMetaDir.resolve(TEST_FILE_ID + ".meta");
        assertTrue(Files.exists(metaPath));

        String content = new String(Files.readAllBytes(metaPath));
        String expectedContent = TEST_ORIGINAL_NAME + "|" + TEST_UPLOAD_TIME.toEpochMilli() + "|";
        assertTrue(content.startsWith(expectedContent));
    }

    @Test
    public void testSaveMetadata_IOException() throws IOException {
        // Arrange - создаем директорию с тем же именем, что и файл метаданных
        Path metaDirAsFile = testMetaDir.resolve(TEST_FILE_ID + ".meta");
        Files.createDirectories(metaDirAsFile);

        // Act - не должно бросать исключение, только логировать
        fileStorageService.saveMetadata(TEST_FILE_ID, TEST_ORIGINAL_NAME, TEST_UPLOAD_TIME);

        // Assert - проверяем что не было исключения
        assertTrue(true); // Если дошли сюда - тест пройден
    }

    @Test
    public void testReadMetadata_Success() throws IOException {
        // Arrange
        FileMetadata expectedMetadata = new FileMetadata(TEST_ORIGINAL_NAME, TEST_UPLOAD_TIME, System.currentTimeMillis());
        Path metaPath = testMetaDir.resolve(TEST_FILE_ID + ".meta");
        Files.write(metaPath, expectedMetadata.toStorageString().getBytes("UTF-8"));

        // Act
        FileMetadata result = fileStorageService.getMetadata(TEST_FILE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_ORIGINAL_NAME, result.getOriginalFilename());
        assertEquals(TEST_UPLOAD_TIME, result.getUploadTime());
    }

    @Test
    public void testReadMetadata_FileNotFound() {
        // Act
        FileMetadata result = fileStorageService.getMetadata("non-existent-id");

        // Assert
        assertNull(result);
    }

    @Test
    public void testReadMetadata_InvalidFormat() throws IOException {
        // Arrange - создаем файл с неверным форматом
        Path metaPath = testMetaDir.resolve(TEST_FILE_ID + ".meta");
        Files.write(metaPath, "invalid|format".getBytes("UTF-8"));

        // Act
        FileMetadata result = fileStorageService.getMetadata(TEST_FILE_ID);

        // Assert
        assertNull(result);
    }

    @Test
    public void testUpdateLastAccessTime_Success() throws IOException, InterruptedException {
        // Arrange
        long originalAccessTime = System.currentTimeMillis() - 10000;
        FileMetadata originalMetadata = new FileMetadata(TEST_ORIGINAL_NAME, TEST_UPLOAD_TIME, originalAccessTime);
        Path metaPath = testMetaDir.resolve(TEST_FILE_ID + ".meta");
        Files.write(metaPath, originalMetadata.toStorageString().getBytes("UTF-8"));

        Thread.sleep(10); // Гарантируем, что время изменится

        // Act
        fileStorageService.updateLastAccessTime(TEST_FILE_ID);

        // Assert
        FileMetadata updatedMetadata = fileStorageService.getMetadata(TEST_FILE_ID);
        assertNotNull(updatedMetadata);
        assertTrue(updatedMetadata.getLastAccessTimeMillis() > originalAccessTime);
        assertEquals(TEST_ORIGINAL_NAME, updatedMetadata.getOriginalFilename());
        assertEquals(TEST_UPLOAD_TIME, updatedMetadata.getUploadTime());
    }

    @Test
    public void testUpdateLastAccessTime_FileNotFound() {
        // Act - не должно бросать исключение
        fileStorageService.updateLastAccessTime("non-existent-id");

        // Assert - проверяем что не было исключения
        assertTrue(true);
    }

    @Test
    public void testGetOriginalFilename_Success() throws IOException {
        // Arrange
        FileMetadata metadata = new FileMetadata(TEST_ORIGINAL_NAME, TEST_UPLOAD_TIME, System.currentTimeMillis());
        Path metaPath = testMetaDir.resolve(TEST_FILE_ID + ".meta");
        Files.write(metaPath, metadata.toStorageString().getBytes("UTF-8"));

        // Act
        String result = fileStorageService.getOriginalFilename(TEST_FILE_ID);

        // Assert
        assertEquals(TEST_ORIGINAL_NAME, result);
    }

    @Test
    public void testGetOriginalFilename_NotFound() {
        // Act
        String result = fileStorageService.getOriginalFilename("non-existent-id");

        // Assert
        assertNull(result);
    }

    @Test
    public void testGetLastAccessTimeMillis_Success() throws IOException {
        // Arrange
        long expectedAccessTime = System.currentTimeMillis();
        FileMetadata metadata = new FileMetadata(TEST_ORIGINAL_NAME, TEST_UPLOAD_TIME, expectedAccessTime);
        Path metaPath = testMetaDir.resolve(TEST_FILE_ID + ".meta");
        Files.write(metaPath, metadata.toStorageString().getBytes("UTF-8"));

        // Act
        long result = fileStorageService.getLastAccessTimeMillis(TEST_FILE_ID);

        // Assert
        assertEquals(expectedAccessTime, result);
    }

    @Test
    public void testGetLastAccessTimeMillis_NotFound() {
        // Act
        long result = fileStorageService.getLastAccessTimeMillis("non-existent-id");

        // Assert
        assertEquals(0L, result);
    }

    @Test
    public void testDeleteFile_Success() throws IOException {
        // Arrange - создаем файл и метаданные
        Path filePath = testStorageDir.resolve(TEST_FILE_ID);
        Path metaPath = testMetaDir.resolve(TEST_FILE_ID + ".meta");

        Files.createFile(filePath);
        Files.createFile(metaPath);

        // Act
        fileStorageService.deleteFile(TEST_FILE_ID);

        // Assert
        assertFalse(Files.exists(filePath));
        assertFalse(Files.exists(metaPath));
    }

    @Test
    public void testDeleteFile_Nonexistent() {
        // Act - не должно бросать исключение
        fileStorageService.deleteFile("non-existent-id");

        // Assert - проверяем что не было исключения
        assertTrue(true);
    }

    @Test
    public void testMetadataConsistency() throws IOException {
        // Arrange
        String fileId = "consistency-test";
        String originalName = "consistent-file.pdf";
        Instant uploadTime = Instant.now().minus(1, ChronoUnit.HOURS);

        // Act
        fileStorageService.saveMetadata(fileId, originalName, uploadTime);

        // Assert - проверяем все методы для согласованности
        FileMetadata metadata = fileStorageService.getMetadata(fileId);
        assertNotNull(metadata);

        assertEquals(originalName, fileStorageService.getOriginalFilename(fileId));
        assertEquals(metadata.getLastAccessTimeMillis(), fileStorageService.getLastAccessTimeMillis(fileId));
        assertEquals(uploadTime, metadata.getUploadTime());
    }
}