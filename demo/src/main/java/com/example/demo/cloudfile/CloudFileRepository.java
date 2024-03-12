package com.example.demo.cloudfile;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CloudFileRepository extends JpaRepository<CloudFile, Long> {
}
