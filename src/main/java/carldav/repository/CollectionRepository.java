package carldav.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import carldav.entity.CollectionItem;

import java.util.List;

/**
 * @author Kamill Sokol
 */
public interface CollectionRepository extends CrudRepository<CollectionItem, Long> {

    @Query("select c from CollectionItem c where c.name = ?2 and c.owner.email = ?1")
    CollectionItem findByCurrentOwnerEmailAndName(String ownerEmail, String name);

    @Query("select c from CollectionItem c where c.name = ?#{ principal.username } and c.owner.email = ?#{ principal.username }")
    CollectionItem findHomeCollectionByCurrentUser();

    @Query("select c from CollectionItem c where c.name = ?1 and c.owner.email = ?1")
    CollectionItem findHomeCollectionByOwnerEmail(String ownerEmail);

    /**
     * @deprecated replace me with {@link CollectionRepository#findByCurrentUser()} as soon as an HTTP DELETE mockMvc test has been added
     */
    @Deprecated
    List<CollectionItem> findByOwnerEmail(String owner);

    @Query("select c from CollectionItem c where c.owner.email = ?#{ principal.username }")
    List<CollectionItem> findByCurrentUser();

    List<CollectionItem> findByParentId(Long id);
}
