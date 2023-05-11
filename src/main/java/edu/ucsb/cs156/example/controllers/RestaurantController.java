package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Restaurant;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RestaurantRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

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


@Api(description = "Restaurants")
@RequestMapping("/api/restaurants")
@RestController
@Slf4j

public class RestaurantController extends ApiController {

    @Autowired
    RestaurantRepository RestaurantRepository;

    @ApiOperation(value = "List all Restaurants")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Restaurant> allRestaurant() {
        Iterable<Restaurant> Restaurants = RestaurantRepository.findAll();
        return Restaurants;
    }

    @ApiOperation(value = "Get a single Restaurant")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Restaurant getById(
            @ApiParam("id") @RequestParam Long id) {
        Restaurant restaurant = RestaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Restaurant.class, id));

        return restaurant;
    }

    @ApiOperation(value = "Create a new Restaurant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Restaurant postRestaurant(
            @ApiParam("phoneNumber") @RequestParam int phoneNumber,
            @ApiParam("city") @RequestParam String city,
            @ApiParam("state") @RequestParam String state)
            throws JsonProcessingException {

        Restaurant restaurant = new Restaurant();
        restaurant.setphoneNumber(phoneNumber);
        restaurant.setCity(city);
        restaurant.setState(state);

        Restaurant savedRestaurant = RestaurantRepository.save(restaurant);

        return savedRestaurant;
    }

    @ApiOperation(value = "Delete a Restaurant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteRestaurant(
            @ApiParam("id") @RequestParam Long id) {
        Restaurant restaurant = RestaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Restaurant.class, id));

        RestaurantRepository.delete(restaurant);
        return genericMessage("Restaurant with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single date")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Restaurant updateRestaurant(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Restaurant incoming) {

        Restaurant restaurant = RestaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Restaurant.class, id));

        restaurant.setphoneNumber(incoming.getphoneNumber());
        restaurant.setCity(incoming.getCity());
        restaurant.setState(incoming.getState());

        RestaurantRepository.save(restaurant);

        return restaurant;
    }
}
