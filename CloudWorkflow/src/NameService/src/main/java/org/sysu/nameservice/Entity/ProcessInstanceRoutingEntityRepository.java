package org.sysu.nameservice.Entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessInstanceRoutingEntityRepository extends JpaRepository<ProcessInstanceRoutingEntity, Long> {
    ProcessInstanceRoutingEntity findById(Long id);
}
