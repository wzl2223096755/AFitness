package com.wzl.fitness.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserProfileRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Min(value = 1, message = "年龄不能小于1")
    @Max(value = 150, message = "年龄不能大于150")
    private Integer age;

    @Positive(message = "体重必须大于0")
    private Double weight;

    @Pattern(regexp = "male|female|other", message = "性别必须是 male, female 或 other")
    private String gender;

    @Positive(message = "身高必须大于0")
    private Integer height;

    private String experienceLevel;
}
