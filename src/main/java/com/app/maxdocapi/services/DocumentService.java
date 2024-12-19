package com.app.maxdocapi.services;

import com.app.maxdocapi.database.entities.Document;
import com.app.maxdocapi.database.repositories.DocumentRepository;
import com.app.maxdocapi.enums.Phase;
import com.app.maxdocapi.exceptions.errors.BadRequestException;
import com.app.maxdocapi.exceptions.errors.NotFoundException;
import com.app.maxdocapi.models.dtos.DocumentCreateDto;
import com.app.maxdocapi.models.projections.AcronymGroupListProjection;
import com.app.maxdocapi.models.projections.DocumentListProjection;
import com.app.maxdocapi.models.records.DocumentEditInfoDto;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    /**
     * Retrieves a paginated list of documents based on the provided filters and sorting criteria.
     * This method allows filtering documents by title, acronym, and phase,
     * and paginates the result according to the specified page number and number of items per page.
     * The results are sorted by the document ID in the specified direction.
     *
     * @param title The title of the document to filter by (can be null or empty for no filtering).
     * @param acronym The acronym of the document to filter by (can be null or empty for no filtering).
     * @param phase The phase of the document to filter by (can be null or empty for no filtering).
     * @param page The page number to retrieve (0-based index).
     * @param itemsPerPage The number of items to retrieve per page.
     * @param sortDirection The direction in which the documents should be sorted (ascending or descending).
     * @return A Page object containing the filtered and paginated list of documents.
     */
    public Page<DocumentListProjection> findAllPaginated(String title, String acronym, String phase, int page, int itemsPerPage, Sort.Direction sortDirection) {
        // Call the repository method to retrieve documents with the provided filters, pagination, and sorting.
        return documentRepository.findAllWithFilters(title, acronym, phase, PageRequest.of(page, itemsPerPage, Sort.by(sortDirection, "id")));
    }


    /**
     * Retrieves a paginated list of documents grouped by acronym.
     * This method allows paginating the results according to the specified page number
     * and number of items per page. The results are sorted by the acronym field
     * in the specified direction.
     *
     * @param page The page number to retrieve (0-based index).
     * @param itemsPerPage The number of items to retrieve per page.
     * @param sortDirection The direction in which the documents should be sorted (ascending or descending).
     * @return A Page object containing the paginated list of documents grouped by acronym.
     */
    public Page<AcronymGroupListProjection> findAllGroupedByAcronym(int page, int itemsPerPage, Sort.Direction sortDirection) {
        // Call the repository method to retrieve documents grouped by acronym with pagination and sorting.
        return documentRepository.findAllGroupedByAcronym(PageRequest.of(page, itemsPerPage, Sort.by(sortDirection, "acronym")));
    }


    /**
     * Retrieves a document by its ID. If the document is not found in the repository,
     * a custom exception is thrown.
     *
     * @param id The ID of the document to be retrieved.
     * @return The document with the specified ID.
     * @throws NotFoundException if no document is found with the given ID.
     */
    public Document findById(Long id) {
        // Tenta buscar o documento no repositório pelo ID fornecido.
        // Se o documento não for encontrado, lança uma exceção NotFoundException.
        return documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Document with id %s not found", id)));
    }


    /**
     * Saves a new document or updates an existing one.
     * If a document with the same acronym and version already exists,
     * a BadRequestException is thrown.
     *
     * @param dto The data transfer object containing the document details.
     * @return The saved document.
     * @throws BadRequestException if a document with the same acronym and version already exists.
     */
    public Document save(DocumentCreateDto dto) {
        // Verifica se já existe um documento com a mesma sigla e versão.
        // Se existir, lança uma exceção BadRequestException.
        if (documentRepository.existsByAcronymAndVersion(dto.getAcronym(), dto.getVersion())) {
            throw new BadRequestException("Já existe um documento nessa versão!");
        }

        // Tenta encontrar o documento existente pelo ID fornecido no DTO.
        // Se o ID não for fornecido ou o documento não for encontrado, cria um novo documento.
        var document = Optional.ofNullable(dto.getId())
                .flatMap(documentRepository::findById) // Tenta recuperar o documento existente pelo ID.
                .orElse(new Document(                 // Se não encontrar, cria um novo documento.
                        null,                           // ID será gerado automaticamente.
                        dto.getTitle(),
                        dto.getDescription(),
                        dto.getAcronym(),
                        dto.getVersion(),
                        Phase.DRAFT                    // Define a fase inicial como DRAFT.
                ));

        // Salva o documento no repositório e retorna o documento salvo.
        return documentRepository.save(document);
    }


    /**
     * Submits a document, changing its phase to ACTIVE.
     * If there is an existing document with the same acronym in the ACTIVE phase,
     * it changes that document's phase to OBSOLETE before submitting the new document.
     *
     * @param id The ID of the document to submit.
     * @return The submitted document with its phase updated to ACTIVE.
     * @throws NotFoundException if no document is found with the given ID.
     */
    @Transactional
    public Document submit(Long id) {
        // Encontra o documento pelo ID.
        var document = findById(id);

        // Verifica se existe um documento ativo com a mesma sigla.
        // Se existir, muda o estado desses documentos ativos para OBSOLETE.
        if (hasActiveDocumentByAcronym(document.getAcronym())) {
            // Recupera todos os documentos com a mesma sigla e filtra apenas os que estão na fase ACTIVE.
            var actives = documentRepository.findAllByAcronym(document.getAcronym())
                    .stream()
                    .filter(it -> it.getPhase().toString().equalsIgnoreCase(Phase.ACTIVE.toString())) // Filtra documentos com a fase ACTIVE.
                    .peek(it -> it.setPhase(Phase.OBSOLETE)) // Altera a fase desses documentos para OBSOLETE.
                    .collect(Collectors.toList());

            // Salva todos os documentos que tiveram sua fase alterada para OBSOLETE.
            documentRepository.saveAll(actives);
        }

        // Define a fase do documento atual como ACTIVE.
        document.setPhase(Phase.ACTIVE);

        // Salva o documento com a nova fase e retorna o documento atualizado.
        return documentRepository.save(document);
    }


    /**
     * Verifica se já existe um documento com a fase ACTIVE para uma sigla específica.
     *
     * @param acronym A sigla do documento a ser verificada.
     * @return true se houver algum documento com a fase ACTIVE com a sigla fornecida;
     *         false caso contrário.
     */
    private boolean hasActiveDocumentByAcronym(String acronym) {
        // Recupera todos os documentos com a sigla fornecida e verifica se algum deles está na fase ACTIVE.
        return documentRepository.findAllByAcronym(acronym)
                .stream()  // Converte a lista de documentos para um fluxo de elementos.
                .anyMatch(it -> it.getPhase().toString().equals(Phase.ACTIVE.toString()));  // Verifica se algum documento tem a fase ACTIVE.
    }


    /**
     * Gera uma nova versão de um documento que está na fase ACTIVE.
     * A nova versão do documento é criada na fase DRAFT com o título, descrição e sigla do documento original,
     * mas com a versão incrementada em 1.
     *
     * @param id O ID do documento para o qual será gerada uma nova versão.
     * @return O novo documento criado, que estará na fase DRAFT com a versão incrementada.
     * @throws BadRequestException Se o documento não estiver na fase ACTIVE ou se ocorrer um erro ao calcular a nova versão.
     */
    @Transactional
    public Document generateVersion(Long id) {
        // Recupera o documento original usando o ID fornecido.
        var document = findById(id);

        // Verifica se o documento está na fase ACTIVE. Se não estiver, lança uma exceção.
        if (!document.getPhase().toString().equalsIgnoreCase(Phase.ACTIVE.toString())) {
            throw new BadRequestException("Only documents with phase ACTIVE can generate version");
        }

        // Recupera a versão máxima dos documentos com a mesma sigla.
        var versionValue = documentRepository.findAllByAcronym(document.getAcronym())
                .stream()  // Converte a lista de documentos para um fluxo.
                .map(Document::getVersion)  // Mapeia os documentos para suas versões.
                .max(Comparator.naturalOrder())  // Encontra o maior número de versão.
                .orElseThrow(() -> new BadRequestException("Erro ao calcular nova versão"));  // Se não houver documentos, lança uma exceção.

        // Cria um novo documento na fase DRAFT, com o título, descrição, sigla e versão incrementada.
        var draftDocument = new Document(
                null,
                document.getTitle(),
                document.getDescription(),
                document.getAcronym(),
                versionValue + 1,  // A versão é incrementada em 1.
                Phase.DRAFT);  // O novo documento começa na fase DRAFT.

        // Persiste o novo documento no repositório e retorna o documento salvo.
        return documentRepository.save(draftDocument);
    }


    /**
     * Edita o título e a descrição de um documento que está na fase DRAFT.
     * Se o documento não estiver na fase DRAFT, lança uma exceção.
     *
     * @param id O ID do documento a ser editado.
     * @param dto O objeto contendo as novas informações para o título e a descrição do documento.
     * @return O documento atualizado com as novas informações.
     * @throws BadRequestException Se o documento não estiver na fase DRAFT.
     */
    @Transactional
    public Document editInfo(Long id, DocumentEditInfoDto dto) {
        // Recupera o documento original usando o ID fornecido.
        var document = findById(id);

        // Verifica se o documento está na fase DRAFT. Se não estiver, lança uma exceção.
        if (!document.getPhase().toString().equalsIgnoreCase(Phase.DRAFT.toString())) {
            throw new BadRequestException("Only documents with phase DRAFT can be edited");
        }

        // Atualiza o título e a descrição do documento com as informações fornecidas no DTO.
        document.setTitle(dto.title());
        document.setDescription(dto.description());

        // Persiste as alterações no repositório e retorna o documento atualizado.
        return documentRepository.save(document);
    }
}
