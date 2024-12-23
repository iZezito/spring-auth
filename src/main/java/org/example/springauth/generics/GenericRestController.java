package org.example.springauth.generics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


public abstract class GenericRestController<T> {

    @Autowired
    public GenericService<T> service;

    @PostMapping
    public T insert(@RequestBody T t) {
        return service.save(t);

    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<T> findById(@PathVariable Long id) {
        T t = service.getById(id);
        if(t == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(t);
    }

    @GetMapping
    public ResponseEntity<List<T>> findAll() {
        List<T> t = service.findAll();
        if(t == null){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(t);
    }

    @PutMapping()
    public ResponseEntity<T> update(@RequestBody T t) {
        service.update(t);
        return ResponseEntity.ok().body(t);
    }

    @DeleteMapping(path = {"/{id}"})
    public ResponseEntity<?> delete(@PathVariable Long id) {
        T t = service.getById(id);
        if(t == null){
            return ResponseEntity.notFound().build();
        }
        service.delete(t);
        return ResponseEntity.ok().build();
    }
}
