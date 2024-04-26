package com.jeipz.aws.s3.file.manager.repository;

import com.jeipz.aws.s3.file.manager.model.SystemFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemFileRepository extends JpaRepository<SystemFile, UUID> {
    Optional<SystemFile> findByFileName(String fileName);
}
