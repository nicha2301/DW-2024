package com.nicha.etl.repository.defaults;

import com.nicha.etl.entity.defaults.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}