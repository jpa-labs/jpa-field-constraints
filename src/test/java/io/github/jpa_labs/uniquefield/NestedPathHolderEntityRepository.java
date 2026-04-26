package io.github.jpa_labs.uniquefield;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface NestedPathHolderEntityRepository extends JpaRepository<NestedPathHolderEntity, UUID> {}
