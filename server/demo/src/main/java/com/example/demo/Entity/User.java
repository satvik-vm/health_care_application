package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "App_User")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int uniqueId;

    @Column(name="email", unique = true, nullable = false)
    private String email;

    @Column(name="password", nullable = false)
    private String password;

    @Column(name = "firstName", columnDefinition = "VARCHAR(255) DEFAULT ''")
    private String firstName = "";

    @Column(name = "lastName", columnDefinition = "VARCHAR(255) DEFAULT ''")
    private String lastName = "";

    @Column(name = "DOB")
    private java.util.Date dob;

    @Column(name = "date_of_joining")
    private Date dateOfJoining;

    @Column(name= "Contact", columnDefinition = "VARCHAR(255) DEFAULT ''")
    private String phone = "";

    @Column(name = "emergencyContact", columnDefinition = "VARCHAR(255) DEFAULT ''")
    private String emContact = "";

    @Column(name= "address", columnDefinition = "VARCHAR(255) DEFAULT ''")
    private String address = "";

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        Role role = this.role;
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        return authorities;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @PrePersist
    protected void onCreate() {
        this.dateOfJoining = new Date(); // Set current date on creation
    }
}