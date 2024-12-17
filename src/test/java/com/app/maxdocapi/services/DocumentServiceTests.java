package com.app.maxdocapi.services;

import com.app.maxdocapi.database.entities.Document;
import com.app.maxdocapi.database.repositories.DocumentRepository;
import com.app.maxdocapi.enums.Phase;
import com.app.maxdocapi.models.dtos.DocumentCreateDto;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class DocumentServiceTests {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @ParameterizedTest
    @MethodSource("provideDocumentCreateDto")
    void assertCanCreateDocument(DocumentCreateDto dto) {
        // Arrange
        documentRepository.deleteAll(); // Garantir estado inicial limpo

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
                Arguments.of(new DocumentCreateDto(null, "Document 1", "Description 1", "DOC1", 1)),
                Arguments.of(new DocumentCreateDto(null, "Document 2", "Description 2", "DOC2", 2)),
                Arguments.of(new DocumentCreateDto(null, "Document 3", "Description 3", "DOC3", 1))
        );
    }

    @Test
    void assertSubmitDocumentChangesPhaseToActive() {
        // Arrange: cria e persiste um documento no estado DRAFT
        Document draftDocument = new Document(null, "Document Title", "Description", "ABC", 1, Phase.DRAFT);
        draftDocument = documentRepository.save(draftDocument);

        // Act: chama o método submit
        Document submittedDocument = documentService.submit(draftDocument.getId());

        // Assert: verifica se o documento foi alterado para ACTIVE
        assertNotNull(submittedDocument);
        assertEquals(Phase.ACTIVE, submittedDocument.getPhase());
    }

    @Test
    void assertSubmitObsoletesExistingActiveDocument() {
        // Arrange: cria e persiste um documento no estado ACTIVE
        Document activeDocument = new Document(null, "Active Document", "Description", "ABC", 1, Phase.ACTIVE);
        activeDocument = documentRepository.save(activeDocument);

        // Cria outro documento no estado DRAFT com a mesma acronym
        Document draftDocument = new Document(null, "Draft Document", "Description", "ABC", 2, Phase.DRAFT);
        draftDocument = documentRepository.save(draftDocument);

        // Act: chama o método submit para o documento no estado DRAFT
        Document submittedDocument = documentService.submit(draftDocument.getId());

        // Assert: verifica se o documento no estado DRAFT foi alterado para ACTIVE
        assertNotNull(submittedDocument);
        assertEquals(Phase.ACTIVE, submittedDocument.getPhase());

        // Verifica se o documento que estava ACTIVE foi alterado para OBSOLETE
        Document updatedActiveDocument = documentRepository.findById(activeDocument.getId()).orElseThrow();
        assertEquals(Phase.OBSOLETE, updatedActiveDocument.getPhase());
    }

    @Test
    void assertSubmitThrowsErrorForNonExistentDocument() {
        // Act & Assert: verifica que tentar submeter um ID inexistente lança uma exceção
        assertThrows(EntityNotFoundException.class, () -> documentService.submit(999L));
    }
}
