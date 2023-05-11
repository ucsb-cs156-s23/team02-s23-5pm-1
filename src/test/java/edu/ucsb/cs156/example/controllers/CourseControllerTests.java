package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Course;
import edu.ucsb.cs156.example.repositories.CourseRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = CourseController.class)
@Import(TestConfig.class)
public class CourseControllerTests extends ControllerTestCase {

        @MockBean
        CourseRepository courseRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/course/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/course/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/course/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/course?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/course/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/course/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/course/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange


                Course course = Course.builder()
                                .title("CS 156")
                                .number(12345)
                                .instructor("Phil Conrad")
                                .build();

                when(courseRepository.findById(eq(7L))).thenReturn(Optional.of(course));

                // act
                MvcResult response = mockMvc.perform(get("/api/course?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(courseRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(course);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(courseRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/course?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(courseRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Course with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_course() throws Exception {

                // arrange


                Course course1 = Course.builder()
                                .title("CS 156")
                                .number(12345)
                                .instructor("Phil Conrad")
                                .build();


                Course course2 = Course.builder()
                                .title("CS 999")
                                .number(77777)
                                .instructor("Joe Schmoe")
                                .build();

                ArrayList<Course> expectedCourses = new ArrayList<>();
                expectedCourses.addAll(Arrays.asList(course1, course2));

                when(courseRepository.findAll()).thenReturn(expectedCourses);

                // act
                MvcResult response = mockMvc.perform(get("/api/course/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(courseRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedCourses);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_course() throws Exception {
                // arrange


                Course course1 = Course.builder()
                                .title("CS156")
                                .number(12345)
                                .instructor("PhilConrad")
                                .build();

                when(courseRepository.save(eq(course1))).thenReturn(course1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/course/post?title=CS156&number=12345&instructor=PhilConrad")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(courseRepository, times(1)).save(course1);
                String expectedJson = mapper.writeValueAsString(course1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_course() throws Exception {
                // arrange


                Course course1 = Course.builder()
                                .title("CS 156")
                                .number(12345)
                                .instructor("Phil Conrad")
                                .build();

                when(courseRepository.findById(eq(15L))).thenReturn(Optional.of(course1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/course?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(courseRepository, times(1)).findById(15L);
                verify(courseRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Course with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_course_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(courseRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/course?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(courseRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Course with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_course() throws Exception {
                // arrange


                Course courseOrig = Course.builder()
                                .title("CS156")
                                .number(12345)
                                .instructor("Phil onrad")
                                .build();

                Course courseEdited = Course.builder()
                                .title("ECE178")
                                .number(23456)
                                .instructor("PradeepSen")
                                .build();

                String requestBody = mapper.writeValueAsString(courseEdited);

                when(courseRepository.findById(eq(67L))).thenReturn(Optional.of(courseOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/course?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(courseRepository, times(1)).findById(67L);
                verify(courseRepository, times(1)).save(courseEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_course_that_does_not_exist() throws Exception {
                // arrange



                Course editedCourse = Course.builder()
                                .title("CS 156")
                                .number(12345)
                                .instructor("Phil Conrad")
                                .build();

                String requestBody = mapper.writeValueAsString(editedCourse);

                when(courseRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/course?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(courseRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Course with id 67 not found", json.get("message"));

        }
}
