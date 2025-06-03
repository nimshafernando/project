package syos.interfaces;

import java.util.List;

/**
 * Gateway interface for data access - Dependency Inversion Principle
 */
public interface IDataGateway<T> {
    T findById(int id);

    List<T> findAll();

    boolean save(T entity);

    boolean update(T entity);

    boolean delete(int id);

    List<T> findByCode(String code);
}
