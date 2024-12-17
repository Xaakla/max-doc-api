package com.app.maxdocapi.services;

import com.app.maxdocapi.database.entities.Document;
import com.app.maxdocapi.database.repositories.DocumentRepository;
import com.app.maxdocapi.enums.Phase;
import com.app.maxdocapi.models.SubmitDto;
import com.app.maxdocapi.models.dtos.DocumentNewEdit;
import com.app.maxdocapi.models.projections.DocumentListProjection;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Page<DocumentListProjection> findAllPaginated(String title, String acronym, Phase phase, int page, int itemsPerPage, Sort.Direction sortDirection) {
        return documentRepository.findAllWithFilters(title, acronym, phase, PageRequest.of(page, itemsPerPage, Sort.by(sortDirection, "id")));
    }

    public Document findById(Long id) {
        return documentRepository.findById(id).orElseThrow(() -> new RuntimeException("dasda"));
    }

    public Document save(DocumentNewEdit dto) {
        var document = Optional.ofNullable(dto.getId())
                .flatMap(documentRepository::findById)
                .orElse(new Document(
                        null,
                        dto.getTitle(),
                        dto.getDescription(),
                        dto.getAcronym(),
                        dto.getVersion(),
                        Phase.DRAFT
                ));


        return documentRepository.save(document);
    }

    @Transactional
    public Document submit(Long id, SubmitDto dto) {
        var documents = documentRepository.findAllByAcronym(dto.acronym());

        if (hasActiveDocumentByAcronym(dto.acronym())) {
            var active = documents.stream().filter(it -> it.getAcronym().equalsIgnoreCase(Phase.ACTIVE.toString())).findFirst().get();
            active.setPhase(Phase.OBSOLETE);
            documentRepository.save(active);
        }

        var submit = documents.stream().filter(it -> it.getId().equals(id)).findFirst().get();
        submit.setPhase(Phase.ACTIVE);
        return documentRepository.save(submit);
    }

    private boolean hasActiveDocumentByAcronym(String acronym) {
        return documentRepository.findAllByAcronym(acronym)
                .stream().anyMatch(it -> it.getAcronym().equals(Phase.ACTIVE.toString()));
    }
}
