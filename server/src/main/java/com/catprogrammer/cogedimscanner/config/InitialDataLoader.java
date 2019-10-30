package com.catprogrammer.cogedimscanner.config;

import com.catprogrammer.cogedimscanner.entity.Privilege;
import com.catprogrammer.cogedimscanner.entity.Role;
import com.catprogrammer.cogedimscanner.entity.User;
import com.catprogrammer.cogedimscanner.repository.PrivilegeRepository;
import com.catprogrammer.cogedimscanner.repository.RoleRepository;
import com.catprogrammer.cogedimscanner.repository.UserRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Component
public class InitialDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetup = false;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialDataLoader(UserRepository userRepository, RoleRepository roleRepository,
                             PrivilegeRepository privilegeRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.privilegeRepository = privilegeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (alreadySetup) {
            return;
        }

        Privilege readPrivilege
                = createPrivilegeIfNotFound("READ_PRIVILEGE");
        Privilege writePrivilege
                = createPrivilegeIfNotFound("WRITE_PRIVILEGE");

        Collection<Privilege> adminPrivileges = Arrays.asList(
                readPrivilege, writePrivilege);
        createRoleIfNotFound("ROLE_ADMIN", adminPrivileges);
        createRoleIfNotFound("ROLE_USER", Collections.singletonList(readPrivilege));

        if (userRepository.findByUsername(Credentials.username) == null) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN");
            User user = new User();
            user.setUsername(Credentials.username);
            user.setPassword(passwordEncoder.encode(Credentials.password));
            user.setRoles(Collections.singletonList(adminRole));
            userRepository.save(user);
        }

        /*User user2 = new User();
        user2.setUsername("user");
        user2.setPassword(passwordEncoder.encode("user"));
        user2.setRoles(Collections.singletonList(roleRepository.findByName("ROLE_USER")));
        userRepository.save(user2);*/

        alreadySetup = true;
    }

    @Transactional
    Privilege createPrivilegeIfNotFound(String name) {

        Privilege privilege = privilegeRepository.findByName(name);
        if (privilege == null) {
            privilege = new Privilege(name);
            privilegeRepository.save(privilege);
        }
        return privilege;
    }

    @Transactional
    void createRoleIfNotFound(
            String name, Collection<Privilege> privileges) {

        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role(name);
            role.setPrivileges(privileges);
            roleRepository.save(role);
        }
    }
}
