package interview.coach.config;

import interview.coach.domain.entity.Role;
import interview.coach.repository.RoleRepository;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoleDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(RoleDataInitializer.class);

    @Bean
    CommandLineRunner seedRoles(RoleRepository roleRepository) {
        return args -> {
            ensureRole(roleRepository, "USER", "Пользователь");
            ensureRole(roleRepository, "ADMIN", "Администратор");
        };
    }

    private void ensureRole(RoleRepository roleRepository, String code, String name) {
        if (roleRepository.findByCode(code).isPresent()) {
            return;
        }

        Role role = new Role();
        role.setCode(code);
        role.setName(name);
        role.setCreatedAt(LocalDateTime.now());
        roleRepository.save(role);
        log.info("Seeded missing role code={}", code);
    }
}
