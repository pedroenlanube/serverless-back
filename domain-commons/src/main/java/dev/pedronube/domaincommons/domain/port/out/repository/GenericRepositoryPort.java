package dev.pedronube.domaincommons.domain.port.out.repository;

import java.util.Optional;

public interface GenericRepositoryPort<T,ID> {
    void save(T entity);
    Optional<T> findById(ID id);
    void deleteById(ID id);
}
