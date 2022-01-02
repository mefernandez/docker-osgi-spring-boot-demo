package org.example.osgi.spring.boot.demo.one.repository;

import org.example.osgi.spring.boot.demo.one.entity.DemoOneEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemoOneRepository extends JpaRepository<DemoOneEntity, Long>{

}
