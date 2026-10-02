package com.vehiclerental.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@AutoConfigureMockMvc
public class VehicleControllerSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testPublicVehicles_AreAccessibleWithoutLogin() throws Exception {
        mockMvc.perform(get("/vehicles")).andExpect(status().isOk());
    }

    @Test
    public void testAdminVehicles_RedirectsAnonymousUsersToLogin() throws Exception {
        mockMvc.perform(get("/admin/vehicles"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    public void testAdminVehicles_Returns403ForCustomer() throws Exception {
        mockMvc.perform(get("/admin/vehicles"))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testAdminVehicles_IsAccessibleForAdmin() throws Exception {
        mockMvc.perform(get("/admin/vehicles"))
               .andExpect(status().isOk());
    }
}
