package com.vehiclerental.security;

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
public class SecurityAccessTests {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.vehiclerental.service.UserService userService;

    @org.junit.jupiter.api.BeforeEach
    public void setup() {
        com.vehiclerental.entity.User mockUser = new com.vehiclerental.entity.User();
        mockUser.setFirstName("Test");
        mockUser.setLastName("User");
        org.mockito.Mockito.when(userService.findByEmail(org.mockito.ArgumentMatchers.anyString())).thenReturn(mockUser);
    }

    @Test
    public void testPublicPages_AreAccessible() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/register")).andExpect(status().isOk());
    }

    @Test
    public void testProtectedPage_RedirectsToLogin_ForUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/customer/dashboard"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    public void testAdminPage_Returns403_ForCustomerRole() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@vehiclerental.com", roles = "ADMIN")
    public void testAdminPage_IsAccessible_ForAdminRole() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
               .andExpect(status().isOk());
    }
}

