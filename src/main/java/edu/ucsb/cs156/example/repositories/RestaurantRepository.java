package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Restaurant;
import edu.ucsb.cs156.example.entities.User;

import org.hibernate.hql.internal.ast.tree.RestrictableStatement;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantRepository extends CrudRepository<Restaurant, Long> {
  
}
