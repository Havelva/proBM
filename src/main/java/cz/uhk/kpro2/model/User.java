package cz.uhk.kpro2.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Username cannot be empty")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    // Password will be handled by UserServiceImpl on save for encoding.
    // For edit, if password field is empty, old password is kept.
    @Column(nullable = false, length = 100)
    private String password;

    @NotEmpty(message = "Roles cannot be empty")
    @Column(nullable = false, length = 50)
    private String roles = "ROLE_USER"; // Default role, can be comma-separated for multiple roles e.g., "ROLE_USER,ROLE_ADMIN"

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }
}