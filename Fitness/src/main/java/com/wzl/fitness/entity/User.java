package com.wzl.fitness.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.wzl.fitness.entity.Role;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_table")
@ToString(exclude = {"trainingRecords", "recoveryMetrics", "trainingAdvices"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    @Column(nullable = false, length = 255)
    private String password;

    @Email(message = "邮箱格式不正确")
    @Column(length = 100)
    private String email;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column
    private Integer age = 25; // 默认年龄25岁

    @Column
    private Double weight = 70.0; // 默认体重70kg

    @Column(name = "gender")
    private String gender;

    @Column(name = "height")
    private Integer height;

    @Column(name = "experience_level")
    private String experienceLevel;

    @Column(name = "avatar")
    private String avatar; // 头像URL或路径

    @Column(name = "points")
    @Builder.Default
    private Integer points = 0; // 用户积分

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    // 训练记录关系
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<TrainingRecord> trainingRecords;
    
    // 恢复指标关系
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<RecoveryMetric> recoveryMetrics;
    
    // 训练建议关系
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<TrainingAdvice> trainingAdvices;
    
    // 手动添加getId方法以确保编译通过
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    // 显式添加其他getter和setter方法
    public Double getWeight() {
        return weight;
    }
    
    public void setWeight(Double weight) {
        this.weight = weight;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public Integer getHeight() {
        return height;
    }
    
    public void setHeight(Integer height) {
        this.height = height;
    }
    
    public String getExperienceLevel() {
        return experienceLevel;
    }
    
    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    // 显式添加builder方法
    public static UserBuilder builder() {
        return new UserBuilder();
    }
    
    // 静态内部Builder类
    public static class UserBuilder {
        private Long id;
        private String username;
        private String password;
        private String email;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime lastLoginAt;
        private Integer age = 25;
        private Double weight = 70.0;
        private String gender;
        private Integer height;
        private String experienceLevel;
        private String avatar;
        private Integer points = 0;
        private Role role = Role.USER;
        
        public UserBuilder id(Long id) {
            this.id = id;
            return this;
        }
        
        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }
        
        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }
        
        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public UserBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public UserBuilder age(Integer age) {
            this.age = age;
            return this;
        }
        
        public UserBuilder weight(Double weight) {
            this.weight = weight;
            return this;
        }
        
        public UserBuilder gender(String gender) {
            this.gender = gender;
            return this;
        }
        
        public UserBuilder height(Integer height) {
            this.height = height;
            return this;
        }
        
        public UserBuilder experienceLevel(String experienceLevel) {
            this.experienceLevel = experienceLevel;
            return this;
        }

        public UserBuilder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public UserBuilder points(Integer points) {
            this.points = points;
            return this;
        }
        
        public UserBuilder role(Role role) {
            this.role = role;
            return this;
        }
        
        public User build() {
            User user = new User();
            user.id = this.id;
            user.username = this.username;
            user.password = this.password;
            user.email = this.email;
            user.createdAt = this.createdAt;
            user.updatedAt = this.updatedAt;
            user.lastLoginAt = this.lastLoginAt;
            user.age = this.age;
            user.weight = this.weight;
            user.gender = this.gender;
            user.height = this.height;
            user.experienceLevel = this.experienceLevel;
            user.avatar = this.avatar;
            user.points = this.points;
            user.role = this.role;
            return user;
        }
    }
}