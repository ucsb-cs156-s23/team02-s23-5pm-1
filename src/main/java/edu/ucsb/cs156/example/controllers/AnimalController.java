package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Animal;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.AnimalRepository;
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

import java.time.LocalDateTime;

@Api(description = "Animals")
@RequestMapping("/api/animals")
@RestController
@Slf4j
public class AnimalController extends ApiController {

    @Autowired
    AnimalRepository animalRepository;

    @ApiOperation(value = "List all animals")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Animal> allAnimal() {
        Iterable<Animal> animals = animalRepository.findAll();
        return animals;
    }

    @ApiOperation(value = "Get a single animal")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Animal getById(
            @ApiParam("id") @RequestParam Long id) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Animal.class, id));

        return animal;
    }

    @ApiOperation(value = "Create a new animal")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Animal postAnimal(
            @ApiParam("name") @RequestParam String name,
            @ApiParam("color") @RequestParam String color,
            @ApiParam("height") @RequestParam int height)
            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters

        Animal animal = new Animal();
        animal.setColor(color);
        animal.setName(name);
        animal.setHeight(height);

        Animal savedAnimal = animalRepository.save(animal);
        return savedAnimal;
    }

    @ApiOperation(value = "Delete a Animal")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteAnimal(
            @ApiParam("id") @RequestParam Long id) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Animal.class, id));

        animalRepository.delete(animal);
        return genericMessage("Animal with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single animal")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Animal updateAnimal(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Animal incoming) {

        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Animal.class, id));

        animal.setColor(incoming.getColor());
        animal.setName(incoming.getName());
        animal.setHeight(incoming.getHeight());

        animalRepository.save(animal);

        return animal;
    }
}
