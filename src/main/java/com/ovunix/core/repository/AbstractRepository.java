package com.ovunix.core.repository;

import com.ovunix.core.domain.Persistable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface AbstractRepository <T extends Persistable, ID extends Serializable> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
}
