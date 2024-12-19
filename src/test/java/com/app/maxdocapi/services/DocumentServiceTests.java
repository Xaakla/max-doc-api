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
        // Act
        Document savedDocument = documentService.save(dto);

        // Assert
        assertNotNull(savedDocument);
        assertEquals(dto.getTitle(), savedDocument.getTitle());
        assertEquals(dto.getDescription(), savedDocument.getDescription());
        assertEquals(dto.getAcronym(), savedDocument.getAcronym());
        assertEquals(dto.getVersion(), savedDocument.getVersion());
        assertEquals(Phase.DRAFT, savedDocument.getPhase()); // Fase inicial esperada
    }

    // Método para fornecer dados ao teste
    private static Stream<Arguments> provideDocumentCreateDto() {
        return Stream.of(
                Arguments.of(getRandomDocumentCreateDto(1)),
                Arguments.of(getRandomDocumentCreateDto(2)),
                Arguments.of(getRandomDocumentCreateDto(1))
        );
    }

    @Test
    void assertSubmitDocumentChangesPhaseToActive() {
        // Arrange: cria e persiste um documento no estado DRAFT
        Document draftDocument = documentRepository.save(getRandomDocument(1, Phase.DRAFT, null));

        // Act: chama o método submit
        Document submittedDocument = documentService.submit(draftDocument.getId());

        // Assert: verifica se o documento foi alterado para ACTIVE
        assertNotNull(submittedDocument);
        assertEquals(Phase.ACTIVE, submittedDocument.getPhase());
    }

    @Test
    void assertSubmitObsoletesExistingActiveDocument() {
        var randomAcronym = RandomStringUtils.randomAlphabetic(5);

        // Arrange: cria e persiste um documento no estado ACTIVE
        Document firstDocument = saveRandomDocument(1, Phase.ACTIVE, randomAcronym);

        // Cria outro documento no estado DRAFT com a mesma acronym
        Document secondDocument = saveRandomDocument(2, Phase.DRAFT, randomAcronym);

        // Act: chama o método submit para o documento no estado DRAFT
        secondDocument = documentService.submit(secondDocument.getId());

        // Assert: verifica se o documento no estado DRAFT foi alterado para ACTIVE
        assertNotNull(secondDocument);
        assertEquals(Phase.ACTIVE, secondDocument.getPhase());

        // Verifica se o documento que estava ACTIVE foi alterado para OBSOLETE
        firstDocument = documentRepository.findById(firstDocument.getId()).orElseThrow();
        assertEquals(Phase.OBSOLETE, firstDocument.getPhase());
    }

    @Test
    void assertSubmitThrowsErrorForNonExistentDocument() {
        // Act & Assert: verifica que tentar submeter um ID inexistente lança uma exceção
        assertThrows(NotFoundException.class, () -> documentService.submit(999L));
    }

    @Test
    void assertGenerateVersionCopiesDocumentWithIncrementedVersion() {
        var activeDocument = saveRandomDocument(1, Phase.ACTIVE, null);

        var draftDocument = documentService.generateVersion(activeDocument.getId());

        assertNotNull(draftDocument);
        assertEquals(activeDocument.getTitle(), draftDocument.getTitle());
        assertEquals(activeDocument.getDescription(), draftDocument.getDescription());
        assertEquals(activeDocument.getAcronym(), draftDocument.getAcronym());
        assertEquals(Phase.DRAFT, draftDocument.getPhase());
        assertEquals(activeDocument.getVersion() + 1, draftDocument.getVersion());
    }

    @Test
    void assertCannotEditNonDraftDocument() {
        var activeDocument = saveRandomDocument(1, Phase.ACTIVE, null);
        var obsoleteDocument = saveRandomDocument(1, Phase.OBSOLETE, null);

        assertThrows(BadRequestException.class, () -> documentService.editInfo(activeDocument.getId(), getRandomDocumentEditInfoDto()));
        assertThrows(BadRequestException.class, () -> documentService.editInfo(obsoleteDocument.getId(), getRandomDocumentEditInfoDto()));
    }

    @Test
    void assertCanOnlyEditTitleAndDescription() {
        var document = saveRandomDocument(1, Phase.DRAFT, null);

        var dto = getRandomDocumentEditInfoDto();

        var editedDocument = documentService.editInfo(document.getId(), dto);

        assertEquals(editedDocument.getTitle(), dto.title());
        assertEquals(editedDocument.getDescription(), dto.description());
        assertEquals(editedDocument.getId(), document.getId());
        assertEquals(editedDocument.getVersion(), document.getVersion());
        assertEquals(editedDocument.getAcronym(), document.getAcronym());
        assertEquals(editedDocument.getPhase(), document.getPhase());
    }

    static Document getRandomDocument(int version, Phase phase, String acronym) {
        return new Document(
                null,
                RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomAlphabetic(20),
                Optional.ofNullable(acronym).orElse(RandomStringUtils.randomAlphabetic(5)),
                version,
                phase);
    }

    static DocumentCreateDto getRandomDocumentCreateDto(int version) {
        return new DocumentCreateDto(
                null,
                RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomAlphabetic(20),
                RandomStringUtils.randomAlphabetic(5),
                version);
    }

    static DocumentEditInfoDto getRandomDocumentEditInfoDto() {
        return new DocumentEditInfoDto(
                RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomAlphabetic(20)
        );
    }

    private Document saveRandomDocument(int version, Phase phase, String acronym) {
        return documentRepository.save(getRandomDocument(version, phase, acronym));
    }
}
