package syos.interfaces;

import java.util.List;

/**
 * Generic Data Access Interface following Interface Segregation Principle
 * 
 * @param <T>  The entity type
 * @param <ID> The ID type
 */
public interface DataAccessInterface<T, ID> {

    /**
     * Insert a new entity
     * 
     * @param entity The entity to insert
     * @return true if successful, false otherwise
     */
    boolean insert(T entity);

    /**
     * Find entity by ID
     * 
     * @param id The entity ID
     * @return The entity if found, null otherwise
     */
    T findById(ID id);

    /**
     * Get all entities
     * 
     * @return List of all entities
     */
    List<T> findAll();

    /**
     * Update an existing entity
     * 
     * @param entity The entity to update
     * @return true if successful, false otherwise
     */
    boolean update(T entity);

    /**
     * Delete an entity by ID
     * 
     * @param id The entity ID
     * @return true if successful, false otherwise
     */
    boolean delete(ID id);
}
