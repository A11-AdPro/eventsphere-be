package id.ac.ui.cs.advprog.eventsphere.report.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    public void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
    }

    @Test
    public void testStoreFile() throws IOException {
        // Create a test file
        MultipartFile file = new MockMultipartFile(
                "testfile",
                "test.txt",
                "text/plain",
                "Test file content".getBytes()
        );

        // Store the file
        String fileName = fileStorageService.storeFile(file);

        // Verify
        assertNotNull(fileName);
        assertTrue(Files.exists(tempDir.resolve(fileName)));
        assertEquals("Test file content", Files.readString(tempDir.resolve(fileName)));
    }

    @Test
    public void testStoreFileWithInvalidName() throws IOException {
        // Create a test file with invalid path
        MultipartFile file = new MockMultipartFile(
                "testfile",
                "../test.txt",
                "text/plain",
                "Test file content".getBytes()
        );

        // Verify that an exception is thrown
        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.storeFile(file);
        });
    }

    @Test
    public void testStoreEmptyFile() {
        // Create an empty file
        MultipartFile file = new MockMultipartFile(
                "emptyfile",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        // Verify that an exception is thrown
        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.storeFile(file);
        });
    }

    @Test
    public void testDeleteFile() throws IOException {
        // Create and store a test file
        MultipartFile file = new MockMultipartFile(
                "testfile",
                "test.txt",
                "text/plain",
                "Test file content".getBytes()
        );

        String fileName = fileStorageService.storeFile(file);

        // Verify the file exists
        assertTrue(Files.exists(tempDir.resolve(fileName)));

        // Delete the file
        fileStorageService.deleteFile(fileName);

        // Verify the file was deleted
        assertFalse(Files.exists(tempDir.resolve(fileName)));
    }

    @Test
    public void testDirectoryCreation() {
        Path validPath = tempDir.resolve("uploads");

        FileStorageService fileStorageService = new FileStorageService(validPath.toString());

        assertTrue(Files.exists(validPath), "The directory should be created successfully.");
    }

    @Test
    public void testConstructorWithInvalidDirectory() {
        // Create a file (not a directory) with the same name as our intended directory
        Path invalidPath = tempDir.resolve("invalid-dir");
        try {
            Files.createFile(invalidPath);

            // Try to initialize FileStorageService with a path that cannot be a directory
            assertThrows(RuntimeException.class, () -> {
                new FileStorageService(invalidPath.toString());
            });
        } catch (IOException e) {
            fail("Test setup failed: " + e.getMessage());
        }
    }

    @Test
    public void testStoreFileWithoutExtension() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "testfile",
                "testWithoutExtension",
                "text/plain",
                "Test content".getBytes()
        );

        String fileName = fileStorageService.storeFile(file);

        assertNotNull(fileName);
        assertFalse(fileName.contains("."));
        assertTrue(Files.exists(tempDir.resolve(fileName)));
    }

    @Test
    public void testStoreFileWithMultipleDots() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "testfile",
                "test.with.multiple.dots.txt",
                "text/plain",
                "Test content".getBytes()
        );

        String fileName = fileStorageService.storeFile(file);

        assertNotNull(fileName);
        assertTrue(fileName.endsWith(".txt"));
        assertTrue(Files.exists(tempDir.resolve(fileName)));
    }
}