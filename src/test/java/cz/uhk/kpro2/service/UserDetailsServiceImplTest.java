package cz.uhk.kpro2.service;

import cz.uhk.kpro2.model.User;
import cz.uhk.kpro2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setRoles("ROLE_USER,ROLE_VIEWER");
    }

    @Test
    void loadUserByUsername_existingUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER")));
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_VIEWER")));
        assertEquals(2, userDetails.getAuthorities().size());

        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_nonExistingUser() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(null);

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistent");
        });

        String expectedMessage = "User not found with username: nonexistent";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    void loadUserByUsername_userWithSingleRole() {
        User singleRoleUser = new User();
        singleRoleUser.setUsername("singlerole");
        singleRoleUser.setPassword("pass");
        singleRoleUser.setRoles("ROLE_ADMIN");
        when(userRepository.findByUsername("singlerole")).thenReturn(singleRoleUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername("singlerole");

        assertNotNull(userDetails);
        assertEquals("singlerole", userDetails.getUsername());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")));
        verify(userRepository, times(1)).findByUsername("singlerole");
    }
}
