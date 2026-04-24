package io.github.jpa_labs.uniquefield;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SampleEntityRepository extends JpaRepository<SampleEntity, UUID> {}
