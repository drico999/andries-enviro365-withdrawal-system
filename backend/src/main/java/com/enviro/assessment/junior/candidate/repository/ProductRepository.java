package com.enviro.assessment.junior.candidate.repository;

import com.enviro.assessment.junior.candidate.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
