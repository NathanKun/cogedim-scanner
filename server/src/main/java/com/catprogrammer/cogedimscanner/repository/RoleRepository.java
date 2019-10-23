package com.catprogrammer.cogedimscanner.repository;

import com.catprogrammer.cogedimscanner.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByName(String name);

    @Override
    void delete(Role role);

}
