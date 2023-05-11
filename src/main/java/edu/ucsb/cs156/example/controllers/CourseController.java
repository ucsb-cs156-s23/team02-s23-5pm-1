package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Course;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.CourseRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

//import java.time.LocalDateTime;

@Api(description = "Course")
@RequestMapping("/api/course")
@RestController
@Slf4j
public class CourseController extends ApiController {

    @Autowired
    CourseRepository courseRepository;

    @ApiOperation(value = "List all courses")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Course> allCourses() {
        Iterable<Course> course = courseRepository.findAll();
        return course;
    }

    @ApiOperation(value = "Get a single course")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Course getById(
            @ApiParam("id") @RequestParam Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Course.class, id));

        return course;
    }

    @ApiOperation(value = "Create a new course")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Course postCourse(
            @ApiParam("title") @RequestParam String title,
            @ApiParam("number") @RequestParam Long number,
            @ApiParam("instructor") @RequestParam String instructor) {
            /*throws JsonProcessingException {

        log.info("localDateTime={}", localDateTime);
*/
        Course course = new Course();
        course.setTitle(title);
        course.setNumber(number);
        course.setInstructor(instructor);

        Course savedCourseDate = courseRepository.save(course);

        return savedCourseDate;
    }

    @ApiOperation(value = "Delete a Course")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteCourse(
            @ApiParam("id") @RequestParam Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Course.class, id));

        courseRepository.delete(course);
        return genericMessage("Course with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single course")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Course updateCourse(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Course incoming) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Course.class, id));

        course.setTitle(incoming.getTitle());
        course.setNumber(incoming.getNumber());
        course.setInstructor(incoming.getInstructor());

        courseRepository.save(course);

        return course;
    }
}
