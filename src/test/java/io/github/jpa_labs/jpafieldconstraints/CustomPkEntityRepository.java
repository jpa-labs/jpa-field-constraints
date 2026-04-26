package io.github.jpa_labs.jpafieldconstraints;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface CustomPkEntityRepository extends JpaRepository<CustomPkEntity, UUID> {}
