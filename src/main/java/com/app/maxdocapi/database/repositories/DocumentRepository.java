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

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, PagingAndSortingRepository<Document, Long> {
    @Query(value = """
            SELECT d FROM Document d
            WHERE
            (:title IS NULL OR :title = '' OR trim(lower(function('unaccent', d.title))) LIKE trim(lower(function('unaccent', concat('%', :title, '%'))))) AND
            (:acronym IS NULL OR :acronym = '' OR trim(lower(function('unaccent', d.acronym))) LIKE trim(lower(function('unaccent', concat('%', :acronym, '%'))))) AND
            (:phase IS NULL OR :phase = '' OR trim(lower(function('unaccent', d.phase))) LIKE trim(lower(function('unaccent', concat('%', :phase, '%')))))
        """)
    Page<DocumentListProjection> findAllWithFilters(@Param("title") String title, @Param("acronym") String acronym, @Param("phase") String phase, Pageable pageable);

    List<Document> findAllByAcronym(String acronym);
}
