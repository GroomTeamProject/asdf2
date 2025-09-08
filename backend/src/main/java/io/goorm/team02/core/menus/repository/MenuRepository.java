// MenuRepository.java
package io.goorm.team02.core.menus.repository;

import io.goorm.team02.core.menus.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByStoreIdOrderByDisplayOrderAsc(Long storeId);

    List<Menu> findByStoreIdAndCategoryIdOrderByDisplayOrderAsc(Long storeId, Long categoryId);

    Optional<Menu> findByIdAndStoreId(Long id, Long storeId);

    boolean existsByStoreIdAndName(Long storeId, String name);
}