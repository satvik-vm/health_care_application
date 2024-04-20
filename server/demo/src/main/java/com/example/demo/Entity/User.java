package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "App_User")
public class User implements UserDetails {
    @Id
    @Column(name="id")
    private String uniqueId;

    @Column(name="email", unique = true, nullable = false)
    private String email;

    @Column(name="password", nullable = false)
    private String password;

    @Column(name = "firstName", columnDefinition = "VARCHAR(255) DEFAULT ''")
    private String firstName = "";

    @Column(name = "lastName", columnDefinition = "VARCHAR(255) DEFAULT ''")
    private String lastName = "";

    @Column(name = "DOB")
    private String dob;

    @Column(name = "Gender")
    private String gender;

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

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "otp_id")
    private Otp otp;

    public User(){
        this.uniqueId = UUID.randomUUID().toString();
    }

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