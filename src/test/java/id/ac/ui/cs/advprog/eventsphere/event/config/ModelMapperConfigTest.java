package id.ac.ui.cs.advprog.eventsphere.event.config;

import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ModelMapperConfigTest {

    @Test
    void modelMapper_shouldReturnModelMapperInstance() {
        // Arrange
        ModelMapperConfig config = new ModelMapperConfig();

        // Act
        ModelMapper modelMapper = config.modelMapper();

        // Assert
        assertNotNull(modelMapper);
    }
}