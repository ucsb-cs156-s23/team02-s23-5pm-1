package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Student;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.StudentRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
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


@Api(description = "Students")
@RequestMapping("/api/students")
@RestController
@Slf4j
public class StudentController extends ApiController {

    @Autowired
    StudentRepository studentRepository;

    @ApiOperation(value = "List all students")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Student> allStudents() {
        Iterable<Student> students = studentRepository.findAll();
        return students;
    }

    @ApiOperation(value = "Get a single stuent")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Student getById(
            @ApiParam("id") @RequestParam long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Student.class, id));

        return student;
    }

    @ApiOperation(value = "Create a new student")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Student postStudent(
        @ApiParam("id") @RequestParam long id,
        @ApiParam("name") @RequestParam String name,
        @ApiParam("year") @RequestParam int year,
        @ApiParam("major") @RequestParam String major
        )
        {

        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setYear(year);
        student.setMajor(major);

        Student savedStudent = studentRepository.save(student);

        return savedStudent;
    }

    @ApiOperation(value = "Delete a student")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteStudent(
            @ApiParam("id") @RequestParam long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Student.class, id));

        studentRepository.delete(student);
        return genericMessage("Student with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single student")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Student updateStudent(
            @ApiParam("id") @RequestParam long id,
            @RequestBody @Valid Student incoming) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Student.class, id));


        student.setId(incoming.getId());
        student.setName(incoming.getName());
        student.setYear(incoming.getYear());
        student.setMajor(incoming.getMajor());

        studentRepository.save(student);

        return student;
    }
}
