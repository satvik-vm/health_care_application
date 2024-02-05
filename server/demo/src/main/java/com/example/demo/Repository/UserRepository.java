package com.example.demo.Repository;

import org.springframework.data.repository.CrudRepository;
import com.example.demo.Entity.User;

public interface UserRepository extends CrudRepository<User, Integer> {
}
