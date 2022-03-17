package com.yuganji.generator.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface OutputRepository extends JpaRepository<Output, Integer> {
    @Transactional
    @Modifying
    @Query(value = "update output set status = :status, ip = :ip where id = :id", nativeQuery = true)
    int setStatus(
            @Param(value = "id") int id,
            @Param(value = "status") int status,
            @Param(value = "ip") String ip);
}
