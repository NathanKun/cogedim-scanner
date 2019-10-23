package com.catprogrammer.cogedimscanner.repository;

import com.catprogrammer.cogedimscanner.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    @Override
    void delete(User user);

}
