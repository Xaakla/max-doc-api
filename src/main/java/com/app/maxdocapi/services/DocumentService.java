package com.app.maxdocapi.services;

import com.app.maxdocapi.database.entities.Document;
import com.app.maxdocapi.database.repositories.DocumentRepository;
import com.app.maxdocapi.models.projections.DocumentListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Page<DocumentListProjection> findAllPaginated(String title, String acronym, String phase, int page, int itemsPerPage, Sort.Direction sortDirection) {
        return documentRepository.findAllWithFilters(title, acronym, phase, PageRequest.of(page, itemsPerPage, Sort.by(sortDirection, "id")));
    }

    public Document findById(Long id) {
        return documentRepository.findById(id).orElseThrow(() -> new RuntimeException("dasda"));
    }
}
