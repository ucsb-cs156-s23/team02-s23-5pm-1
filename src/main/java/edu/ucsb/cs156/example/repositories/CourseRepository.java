package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Course;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CourseRepository extends CrudRepository<Course, Long> {
  //Iterable<Course> findAllByQuarterYYYYQ(String quarterYYYYQ);  [FIXME?]
}