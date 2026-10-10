package com.chococraft.repository;

import com.chococraft.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findByActiveTrue();

    List<Product> findByFeaturedTrueAndActiveTrue();

    List<Product> findByBestsellerTrueAndActiveTrue();

    List<Product> findByStockQuantityLessThanEqual(Integer threshold);

    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:query IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> filterProducts(@Param("categoryId") Long categoryId,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 @Param("query") String query,
                                 Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.id != :productId AND p.active = true")
    List<Product> findRelatedProducts(@Param("categoryId") Long categoryId, @Param("productId") Long productId, Pageable pageable);
}
