package org.sysu.nameservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sysu.nameservice.Entity.ProcessInstanceRoutingEntity;
import org.sysu.nameservice.Entity.ProcessInstanceRoutingEntityRepository;

/**
 * 如果需要实现缓存，应该这里实现
 */
@Service
public class ProcessInstanceRoutingService {
    @Autowired
    private ProcessInstanceRoutingEntityRepository processInstanceRoutingTableEntityRepository;

    public ProcessInstanceRoutingEntity findById(Long id) {
        return processInstanceRoutingTableEntityRepository.findById(id);
    }

    public ProcessInstanceRoutingEntity saveOrUpdate(ProcessInstanceRoutingEntity processInstanceRoutingEntity) {
        return processInstanceRoutingTableEntityRepository.save(processInstanceRoutingEntity);
    }


}
