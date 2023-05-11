package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Student;
import edu.ucsb.cs156.example.repositories.StudentRepository;

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

@WebMvcTest(controllers = StudentController.class)
@Import(TestConfig.class)
public class StudentControllerTests extends ControllerTestCase {

        @MockBean
        StudentRepository studentRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/student/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/students/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/students/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/students?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/student/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/students/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/students/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                
                Student student = Student.builder()
                                .id(1)
                                .name("Elijah")
                                .major("Computer Science")
                                .year(2)
                                .build();

                when(studentRepository.findById(eq(1L))).thenReturn(Optional.of(student));

                // act
                MvcResult response = mockMvc.perform(get("/api/students?id=1"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(studentRepository, times(1)).findById(eq(1L));
                String expectedJson = mapper.writeValueAsString(student);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(studentRepository.findById(eq(999L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/students?id=999"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(studentRepository, times(1)).findById(eq(999L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Student with id 999 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_students() throws Exception {

                // arrange

                Student jack = Student.builder()
                                .name("Jack")
                                .id(2)
                                .major("Sociology")
                                .year(3)
                                .build();

                
                Student jill = Student.builder()
                                .name("Jill")
                                .id(3)
                                .major("Biology")
                                .year(4)
                                .build();

                ArrayList<Student> expectedStudents = new ArrayList<>();
                expectedStudents.addAll(Arrays.asList(jack, jill));

                when(studentRepository.findAll()).thenReturn(expectedStudents);

                // act
                MvcResult response = mockMvc.perform(get("/api/students/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(studentRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedStudents);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_student() throws Exception {
                // arrange

                Student nat = Student.builder()
                                .name("Nat")
                                .id(4)
                                .major("Chemistry")
                                .year(2)
                                .build();

                when(studentRepository.save(eq(nat))).thenReturn(nat);

                // act
                MvcResult response = mockMvc.perform(
                    post("/api/students/post?id=4&name=Nat&major=Chemistry&year=2")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(studentRepository, times(1)).save(nat);
                String expectedJson = mapper.writeValueAsString(nat);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_student() throws Exception {
                // arrange

                Student alex = Student.builder()
                                .name("Alex")
                                .id(5)
                                .major("International Studies")
                                .year(1)
                                .build();

                when(studentRepository.findById(eq(5L))).thenReturn(Optional.of(alex));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/students?id=5")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(studentRepository, times(1)).findById(eq(5L));
                verify(studentRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Student with id 5 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_student_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(studentRepository.findById(eq(999L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/students?id=999")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(studentRepository, times(1)).findById(eq(999L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("Student with id 999 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_student() throws Exception {
                // arrange

                Student studentOrig = Student.builder()
                                .id(1)
                                .name("Elijah")
                                .major("Computer Science")
                                .year(2)
                                .build();

                Student studentEdited = Student.builder()
                                .id(1)
                                .name("Elijah Frankle")
                                .major("Computer Science")
                                .year(2)
                                .build();

                String requestBody = mapper.writeValueAsString(studentEdited);

                when(studentRepository.findById(eq(1L))).thenReturn(Optional.of(studentOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/students?id=1")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(studentRepository, times(1)).findById(eq(1L));
                verify(studentRepository, times(1)).save(studentEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_student_that_does_not_exist() throws Exception {
                // arrange

                Student editedStudent = Student.builder()
                                .id(999)
                                .name("Bob")
                                .major("Magic")
                                .year(10)
                                .build();

                String requestBody = mapper.writeValueAsString(editedStudent);

                when(studentRepository.findById(eq(999L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/students?id=999")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(studentRepository, times(1)).findById(eq(999L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("Student with id 999 not found", json.get("message"));

        }
}
