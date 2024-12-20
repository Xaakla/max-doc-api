package com.app.maxdocapi.controllers;

import com.app.maxdocapi.Routes;
import com.app.maxdocapi.common.ResponseResult;
import com.app.maxdocapi.common.ResultPageDto;
import com.app.maxdocapi.models.dtos.DocumentCreateDto;
import com.app.maxdocapi.models.projections.AcronymGroupListProjection;
import com.app.maxdocapi.models.projections.DocumentListProjection;
import com.app.maxdocapi.models.dtos.DocumentListDto;
import com.app.maxdocapi.models.records.DocumentEditInfoDto;
import com.app.maxdocapi.services.DocumentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(final DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping(value = Routes.Documents.path)
    public ResponseResult<ResultPageDto<DocumentListProjection, DocumentListProjection>> findAllPaginated(
            @RequestParam(required = false, defaultValue = "") String title,
            @RequestParam(required = false, defaultValue = "") String acronym,
            @RequestParam(required = false, defaultValue = "") String phase,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int itemsPerPage,
            @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sortDirection
    ) {
        return ResponseResult.success(new ResultPageDto<>(documentService.findAllPaginated(title, acronym, phase, page, itemsPerPage, sortDirection)));
    }

    @GetMapping(value = Routes.Documents.Acronym.path)
    public ResponseResult<ResultPageDto<AcronymGroupListProjection, AcronymGroupListProjection>> findAllGroupedByAcronym(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int itemsPerPage,
            @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sortDirection
    ) {
        return ResponseResult.success(new ResultPageDto<>(documentService.findAllGroupedByAcronym(page, itemsPerPage, sortDirection)));
    }

    @GetMapping(value = Routes.Documents.ById.path)
    public ResponseResult<DocumentListDto> findById(@PathVariable Long id) {
        return ResponseResult.success(new DocumentListDto(documentService.findById(id)));
    }

    @PostMapping(value = Routes.Documents.path)
    public ResponseResult<DocumentListDto> save(@RequestBody @Valid DocumentCreateDto dto) {
        return ResponseResult.success(new DocumentListDto(documentService.save(dto)));
    }

    @PatchMapping(value = Routes.Documents.ById.Submit.path)
    public ResponseResult<DocumentListDto> submit(@PathVariable Long id) {
        return ResponseResult.success(new DocumentListDto(documentService.submit(id)));
    }

    @PostMapping(value = Routes.Documents.ById.GenerateVersion.path)
    public ResponseResult<DocumentListDto> generateVersion(@PathVariable Long id) {
        return ResponseResult.success(new DocumentListDto(documentService.generateVersion(id)));
    }

    @PatchMapping(value = Routes.Documents.ById.path)
    public ResponseResult<DocumentListDto> editInfo(@PathVariable Long id, @RequestBody @Valid DocumentEditInfoDto dto) {
        return ResponseResult.success(new DocumentListDto(documentService.editInfo(id, dto)));
    }
}
