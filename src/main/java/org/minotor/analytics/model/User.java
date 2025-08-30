package org.minotor.analytics.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private Long companyId;

    // Constructeurs
    public User() {}

    public User(Long id, String email, String firstName, String lastName, String role, Long companyId) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.companyId = companyId;
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @JsonProperty("companyId")
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    // Méthodes utilitaires
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isSales() {
        return "Sales".equals(role);
    }

    public boolean isAdmin() {
        return "Admin".equals(role);
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, email='%s', name='%s %s', role='%s', companyId=%d}",
                id, email, firstName, lastName, role, companyId);
    }
}