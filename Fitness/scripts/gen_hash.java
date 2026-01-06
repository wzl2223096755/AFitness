///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.springframework.security:spring-security-crypto:6.2.0

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class gen_hash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "Test123!";
        String hash = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
    }
}
