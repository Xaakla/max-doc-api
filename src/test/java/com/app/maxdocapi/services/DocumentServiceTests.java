package com.app.maxdocapi.services;

import com.app.maxdocapi.database.entities.Document;
import com.app.maxdocapi.database.repositories.DocumentRepository;
import com.app.maxdocapi.enums.Phase;
import com.app.maxdocapi.exceptions.errors.BadRequestException;
import com.app.maxdocapi.exceptions.errors.NotFoundException;
import com.app.maxdocapi.models.dtos.DocumentCreateDto;
import com.app.maxdocapi.models.records.DocumentEditInfoDto;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import io.zonky.test.db.util.RandomStringUtils;
import jakarta.persistence.EntityNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureEmbeddedDatabase
public class DocumentServiceTests {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @ParameterizedTest
    @MethodSource("provideDocumentCreateDto")
    void assertCanCreateDocument(DocumentCreateDto dto) {
        // Act: Save the document using the service.
        // Calls the save() method of documentService to persist the DocumentCreateDto into the database or repository.
        Document savedDocument = documentService.save(dto);

        // Assert: Verify the document is saved correctly and initialized as expected.
        // Ensures that the saved document is not null.
        assertNotNull(savedDocument);

        // Validates that the title of the saved document matches the title provided in the DTO.
        assertEquals(dto.getTitle(), savedDocument.getTitle());

        // Validates that the description of the saved document matches the description provided in the DTO.
        assertEquals(dto.getDescription(), savedDocument.getDescription());

        // Validates that the acronym of the saved document matches the acronym provided in the DTO.
        assertEquals(dto.getAcronym(), savedDocument.getAcronym());

        // Validates that the version of the saved document matches the version provided in the DTO.
        assertEquals(dto.getVersion(), savedDocument.getVersion());

        // Verifies that the initial phase of the saved document is set to DRAFT as per the business logic.
        assertEquals(Phase.DRAFT, savedDocument.getPhase());
    }

    /**
     * Provides a stream of test cases for the parameterized test.
     * Each test case is an instance of DocumentCreateDto with varying data.
     *
     * @return Stream of Arguments containing DocumentCreateDto objects.
     */
    private static Stream<Arguments> provideDocumentCreateDto() {
        return Stream.of(
                // Generates a DocumentCreateDto object with a random set of data and ID 1.
                Arguments.of(getRandomDocumentCreateDto(1)),

                // Generates a DocumentCreateDto object with a random set of data and ID 2.
                Arguments.of(getRandomDocumentCreateDto(2)),

                // Generates a DocumentCreateDto object with a random set of data and ID 1 again (repeated scenario).
                Arguments.of(getRandomDocumentCreateDto(1))
        );
    }


    @Test
    void assertSubmitDocumentChangesPhaseToActive() {
        // Arrange: Create and persist a document in the DRAFT phase.
        // Simulates the initial state of the document as DRAFT, saved in the repository for testing.
        Document draftDocument = documentRepository.save(getRandomDocument(1, Phase.DRAFT, null));

        // Act: Call the submit method to transition the document's phase.
        // This action simulates submitting the document, which should update its phase to ACTIVE.
        Document submittedDocument = documentService.submit(draftDocument.getId());

        // Assert: Verify that the document's phase has been changed to ACTIVE.
        // Ensures the returned document is not null, indicating a successful transition.
        assertNotNull(submittedDocument);

        // Confirms that the document's phase has been updated correctly to ACTIVE.
        assertEquals(Phase.ACTIVE, submittedDocument.getPhase());
    }


    @Test
    void assertSubmitObsoletesExistingActiveDocument() {
        // Arrange: Generate a random acronym to ensure unique document grouping.
        var randomAcronym = RandomStringUtils.randomAlphabetic(5);

        // Arrange: Create and persist a document in the ACTIVE phase with the generated acronym.
        // This simulates an existing active document in the system.
        Document firstDocument = saveRandomDocument(1, Phase.ACTIVE, randomAcronym);

        // Arrange: Create and persist another document in the DRAFT phase with the same acronym.
        // This represents a new version of the document that is being prepared.
        Document secondDocument = saveRandomDocument(2, Phase.DRAFT, randomAcronym);

        // Act: Call the submit method for the document in the DRAFT phase.
        // This action should transition the DRAFT document to ACTIVE and obsolete the previous ACTIVE document.
        secondDocument = documentService.submit(secondDocument.getId());

        // Assert: Verify that the DRAFT document has been transitioned to the ACTIVE phase.
        // Ensures the phase of the submitted document is updated correctly.
        assertNotNull(secondDocument);
        assertEquals(Phase.ACTIVE, secondDocument.getPhase());

        // Assert: Verify that the original ACTIVE document has been transitioned to the OBSOLETE phase.
        // Ensures only one document remains in the ACTIVE phase per acronym.
        firstDocument = documentRepository.findById(firstDocument.getId()).orElseThrow();
        assertEquals(Phase.OBSOLETE, firstDocument.getPhase());
    }


    @Test
    void assertSubmitThrowsErrorForNonExistentDocument() {
        // Act & Assert: Verify that attempting to submit a document with a non-existent ID throws a NotFoundException.
        // The ID 999L is used as a placeholder for a document that does not exist in the database.
        assertThrows(NotFoundException.class, () -> documentService.submit(999L));
    }


    @Test
    void assertGenerateVersionCopiesDocumentWithIncrementedVersion() {
        // Arrange: Save an active document to the repository for testing.
        // This document will be the base for generating a new version.
        var activeDocument = saveRandomDocument(1, Phase.ACTIVE, null);

        // Act: Call the generateVersion method to create a new draft version of the document.
        var draftDocument = documentService.generateVersion(activeDocument.getId());

        // Assert: Ensure the new draft document is not null.
        assertNotNull(draftDocument);

        // Assert: Verify that the title of the draft matches the title of the active document.
        assertEquals(activeDocument.getTitle(), draftDocument.getTitle());

        // Assert: Verify that the description of the draft matches the description of the active document.
        assertEquals(activeDocument.getDescription(), draftDocument.getDescription());

        // Assert: Verify that the acronym of the draft matches the acronym of the active document.
        assertEquals(activeDocument.getAcronym(), draftDocument.getAcronym());

        // Assert: Verify that the phase of the new document is DRAFT, as expected.
        assertEquals(Phase.DRAFT, draftDocument.getPhase());

        // Assert: Ensure that the version of the draft document is incremented by 1 compared to the active document.
        assertEquals(activeDocument.getVersion() + 1, draftDocument.getVersion());
    }


    @Test
    void assertCannotEditNonDraftDocument() {
        // Arrange: Save two documents with different phases (ACTIVE and OBSOLETE) to the repository.
        // These documents will be used to test that only DRAFT documents can be edited.
        var activeDocument = saveRandomDocument(1, Phase.ACTIVE, null);
        var obsoleteDocument = saveRandomDocument(1, Phase.OBSOLETE, null);

        // Act & Assert: Verify that attempting to edit an ACTIVE document throws a BadRequestException.
        assertThrows(BadRequestException.class,
                () -> documentService.editInfo(activeDocument.getId(), getRandomDocumentEditInfoDto()));

        // Act & Assert: Verify that attempting to edit an OBSOLETE document also throws a BadRequestException.
        assertThrows(BadRequestException.class,
                () -> documentService.editInfo(obsoleteDocument.getId(), getRandomDocumentEditInfoDto()));
    }


    @Test
    void assertCanOnlyEditTitleAndDescription() {
        // Arrange: Create and save a document in the DRAFT phase for testing.
        // This document will be edited to verify that only the title and description can be changed.
        var document = saveRandomDocument(1, Phase.DRAFT, null);

        // Create a DTO with new title and description to simulate the edit request.
        var dto = getRandomDocumentEditInfoDto();

        // Act: Call the editInfo method to apply changes to the document.
        // This simulates editing the document's title and description.
        var editedDocument = documentService.editInfo(document.getId(), dto);

        // Assert: Verify that the title and description were updated correctly.
        assertEquals(editedDocument.getTitle(), dto.title());
        assertEquals(editedDocument.getDescription(), dto.description());

        // Assert: Verify that the ID, version, acronym, and phase remain unchanged.
        assertEquals(editedDocument.getId(), document.getId());
        assertEquals(editedDocument.getVersion(), document.getVersion());
        assertEquals(editedDocument.getAcronym(), document.getAcronym());
        assertEquals(editedDocument.getPhase(), document.getPhase());
    }


    /**
     * Creates a random document with the given version, phase, and acronym.
     * If acronym is not provided, a random acronym will be generated.
     *
     * @param version The version of the document to be created.
     * @param phase The phase of the document (e.g., DRAFT, ACTIVE, OBSOLETE).
     * @param acronym The acronym of the document, or null to generate a random acronym.
     * @return A new Document object with random title, description, acronym, and the given version and phase.
     */
    static Document getRandomDocument(int version, Phase phase, String acronym) {
        // Generate a new Document with random title, description, and acronym (or provided acronym).
        return new Document(
                null, // Document ID is set to null, indicating it hasn't been persisted yet.
                RandomStringUtils.randomAlphabetic(10), // Random 10-character string for the title.
                RandomStringUtils.randomAlphabetic(20), // Random 20-character string for the description.
                Optional.ofNullable(acronym).orElse(RandomStringUtils.randomAlphabetic(5)), // Use provided acronym or generate a random 5-character string.
                version, // The version provided by the caller.
                phase); // The phase provided by the caller (e.g., DRAFT, ACTIVE, OBSOLETE).
    }


    /**
     * Creates a random DocumentCreateDto with the given version.
     * The DTO contains random title, description, and acronym.
     *
     * @param version The version of the document to be created.
     * @return A new DocumentCreateDto with random title, description, acronym, and the given version.
     */
    static DocumentCreateDto getRandomDocumentCreateDto(int version) {
        // Create a new DocumentCreateDto with random title, description, and acronym.
        return new DocumentCreateDto(
                null, // ID is set to null as this is for creating a new document.
                RandomStringUtils.randomAlphabetic(10), // Random 10-character string for the title.
                RandomStringUtils.randomAlphabetic(20), // Random 20-character string for the description.
                RandomStringUtils.randomAlphabetic(5), // Random 5-character string for the acronym.
                version); // The version passed as a parameter.
    }


    /**
     * Creates a random DocumentEditInfoDto with random title and description.
     * This DTO is used to simulate editing a document's title and description.
     *
     * @return A new DocumentEditInfoDto with random title and description.
     */
    static DocumentEditInfoDto getRandomDocumentEditInfoDto() {
        // Create a new DocumentEditInfoDto with random title and description.
        return new DocumentEditInfoDto(
                RandomStringUtils.randomAlphabetic(10), // Random 10-character string for the title.
                RandomStringUtils.randomAlphabetic(20)  // Random 20-character string for the description.
        );
    }


    /**
     * Saves a randomly generated Document to the repository.
     * This method is used to create and persist a document with random data
     * in the specified version, phase, and acronym.
     *
     * @param version The version of the document.
     * @param phase The phase of the document (e.g., DRAFT, ACTIVE, OBSOLETE).
     * @param acronym The acronym for the document, or null to generate a random one.
     * @return The saved Document object with generated data.
     */
    private Document saveRandomDocument(int version, Phase phase, String acronym) {
        // Generate a random document with the specified version, phase, and acronym.
        Document document = getRandomDocument(version, phase, acronym);

        // Save the generated document to the repository and return the saved entity.
        return documentRepository.save(document);
    }

}
