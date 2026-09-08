package com.enviro.assessment.junior.andries.repository;

import com.enviro.assessment.junior.andries.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
