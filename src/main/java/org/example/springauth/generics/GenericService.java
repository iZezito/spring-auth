package org.example.springauth.generics;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;


public abstract class GenericService<T>{
    protected GenericRepository<T> repository;

    public T save(T t){
        return repository.save(t);
    }

    public void delete(T t){
        repository.delete(t);
    }

    public List<T> findAll(){
        return repository.findAll();
    }

    public T update(T t){
        return repository.saveAndFlush(t);
    }

    public T getById(Long id){
        Optional<T> object = repository.findById(id);
        return object.orElse(null);
    }
}
