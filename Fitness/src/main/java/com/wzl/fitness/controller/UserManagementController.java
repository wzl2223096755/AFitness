package com.wzl.fitness.controller;

import com.wzl.fitness.annotation.RequireAdmin;
import com.wzl.fitness.annotation.RequireUser;
import com.wzl.fitness.common.ApiResponse;
import com.wzl.fitness.common.BaseController;
import com.wzl.fitness.dto.request.RegisterRequest;
import com.wzl.fitness.entity.User;
import com.wzl.fitness.exception.BusinessException;
import com.wzl.fitness.service.AuditLogService;
import com.wzl.fitness.service.AuthenticationService;
import com.wzl.fitness.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "用户管理（管理员）", description = "管理员用户管理接口，包括用户列表、添加、删除等")
public class UserManagementController extends BaseController {
    
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final AuditLogService auditLogService;
    
    /**
     * 获取所有用户列表 - 需要管理员权限
     */
    @GetMapping
    @RequireAdmin
    @Operation(summary = "获取所有用户", description = "获取系统中所有用户的列表（需要管理员权限）")
    public ApiResponse<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ApiResponse.success(users);
    }
    
    /**
     * 根据ID获取用户详情 - 需要管理员权限
     */
    @GetMapping("/{id}")
    @RequireAdmin
    @Operation(summary = "获取用户详情", description = "根据ID获取用户详细信息（需要管理员权限）")
    public ApiResponse<User> getUserById(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        return userService.getUserById(id)
                .map(ApiResponse::success)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    @RequireUser
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的信息")
    public ApiResponse<User> getCurrentUser(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            User user = authenticationService.getUserById(userId);
            return ApiResponse.success(user);
        } catch (BusinessException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "获取用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 管理员添加用户
     */
    @PostMapping("/add")
    @RequireAdmin
    @Operation(summary = "添加用户", description = "管理员添加新用户（需要管理员权限）")
    public ApiResponse<User> addUser(@Valid @RequestBody RegisterRequest registerRequest, 
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ApiResponse.error(400, errorMessage);
        }
        
        try {
            // 注意：这里可能需要转换 RegisterRequest DTO 类型，因为两个 Service 使用的 DTO 可能不同
            // 如果两个 Service 使用的是同一个 DTO 类，则可以直接调用
            authenticationService.register(registerRequest);
            User user = authenticationService.getUserByUsername(registerRequest.getUsername());
            return ApiResponse.success("用户添加成功", user);
        } catch (BusinessException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "添加用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户信息 - 需要管理员权限
     */
    @PutMapping("/{id}")
    @RequireAdmin
    @Operation(summary = "更新用户信息", description = "更新指定用户的信息（需要管理员权限）")
    public ApiResponse<User> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id, 
            @RequestBody User userDetails) {
        try {
            return userService.updateUser(id, userDetails)
                    .map(updatedUser -> ApiResponse.success("用户信息更新成功", updatedUser))
                    .orElseThrow(() -> new BusinessException("用户不存在"));
        } catch (BusinessException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "更新用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户 - 需要管理员权限
     */
    @DeleteMapping("/{id}")
    @RequireAdmin
    @Operation(summary = "删除用户", description = "删除指定用户（需要管理员权限）")
    public ApiResponse<String> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long id, 
            HttpServletRequest request) {
        try {
            // 获取要删除的用户信息用于审计日志
            User userToDelete = userService.getUserById(id)
                    .orElseThrow(() -> new BusinessException("用户不存在"));
            String deletedUsername = userToDelete.getUsername();
            
            // 获取当前管理员信息
            Long adminUserId = getUserIdFromRequest(request);
            User adminUser = userService.getUserById(adminUserId)
                    .orElseThrow(() -> new BusinessException("管理员用户不存在"));
            
            boolean deleted = userService.deleteUser(id);
            if (!deleted) {
                throw new BusinessException("用户不存在");
            }
            
            // 记录管理员操作审计日志
            auditLogService.logAdminAction(adminUserId, adminUser.getUsername(), 
                    "删除用户: " + deletedUsername, "用户", id);
            
            return ApiResponse.success("用户删除成功");
        } catch (BusinessException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "删除用户失败: " + e.getMessage());
        }
    }
}
