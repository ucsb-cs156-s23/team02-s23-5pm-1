package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ucsb.cs156.example.entities.Book;
import edu.ucsb.cs156.example.repositories.BookRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(description="Book information (admin only)")
@RequestMapping("/api/admin/books")
@RestController
public class BooksController extends ApiController {
    @Autowired
    BookRepository bookRepository;

    @Autowired
    ObjectMapper mapper;

    @ApiOperation(value = "Get a list of all books")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("")
    public ResponseEntity<String> books()
            throws JsonProcessingException {
        Iterable<Book> books = bookRepository.findAll();
        String body = mapper.writeValueAsString(books);
        return ResponseEntity.ok().body(body);
    }
}