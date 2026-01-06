import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具
 * 用于生成测试账户的BCrypt密码哈希
 * 
 * 编译运行:
 * javac -cp "target/classes:target/dependency/*" scripts/GeneratePassword.java
 * java -cp "target/classes:target/dependency/*:scripts" GeneratePassword
 */
public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String[] passwords = {
            "Admin@123",
            "Test@123", 
            "Fitness@2024",
            "Gym#Master1",
            "Health$Pro99"
        };
        
        System.out.println("=== BCrypt Password Hashes ===");
        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println(password + " -> " + hash);
        }
    }
}
