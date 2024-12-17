package com.app.maxdocapi.database.repositories;

import com.app.maxdocapi.database.entities.Document;
import com.app.maxdocapi.models.projections.DocumentListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, PagingAndSortingRepository<Document, Long> {
    @Query(value = """
            SELECT d FROM documents d
            WHERE
                (:title IS NULL OR :title = ''
                    OR :acronym IS NULL OR :acronym = ''
                    OR :phase IS NULL OR :phase = ''
                    OR unaccent(lower(d.title)) LIKE (lower(CONCAT('%', :title, '%')))
                    OR unaccent(lower(d.acronym)) LIKE (lower(CONCAT('%', :acronym, '%')))
                    OR unaccent(lower(d.phase)) LIKE (lower(CONCAT('%', :phase, '%'))))
        """, nativeQuery = true)
    Page<DocumentListProjection> findAllWithFilters(@Param("title") String title, @Param("acronym") String acronym, @Param("phase") String phase, Pageable pageable);
}
