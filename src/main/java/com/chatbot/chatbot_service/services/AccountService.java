package com.chatbot.chatbot_service.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.chatbot.chatbot_service.DTOs.AccountProfile;
import com.chatbot.chatbot_service.DTOs.ChangePasswordDTO;
import com.chatbot.chatbot_service.DTOs.ConfirmTokenRequest;
import com.chatbot.chatbot_service.DTOs.ForgetPassword;
import com.chatbot.chatbot_service.DTOs.ProfileUpdate;
import com.chatbot.chatbot_service.DTOs.RegisterRequest;
import com.chatbot.chatbot_service.DTOs.ResetPasswordRequest;
import com.chatbot.chatbot_service.models.Account;
import com.chatbot.chatbot_service.models.Session;
import com.chatbot.chatbot_service.repositories.AccountRepository;
import com.chatbot.chatbot_service.repositories.SessionRepository;
import com.chatbot.chatbot_service.util.TokenGenerator;

@Service
public class AccountService implements UserDetailsService {
    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

        private void sendResetTokenToEmail(String email, String token) {
            emailService.sendResetTokenToEmail(email, token);
        }

        public ResponseEntity<?> save(RegisterRequest registerRequest){
        if(!accountRepository.existsByUserName(registerRequest.getUserName())){
            if(!accountRepository.existsByEmail(registerRequest.getEmail())){
                Account account = new Account(registerRequest);
                account.setPassword(passwordEncoder.encode(account.getPassword()));
                account.setCreatedAt(LocalDateTime.now());
                accountRepository.save(account);
                return ResponseEntity.ok("Account Created Successfully!");
            }
            return ResponseEntity.badRequest().body("Email is already in use!");
        }
        return ResponseEntity.badRequest().body("Username already exists!");
    }

    public Account findByUsername(String username) {
        return accountRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found!"));
    }

        @Override
        public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        Account account = findByUsername(userName);
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        return new User(account.getUserName(), account.getPassword(), authorities);
    }

        public ResponseEntity<?> forgetPassword(ForgetPassword forgetPassword) {
            Optional<Account> accounts = accountRepository.findByUserName(forgetPassword.getUserName());
            if(!accounts.isPresent()){
                return ResponseEntity.badRequest().body("User not found!");
            }
            if(!accounts.get().getEmail().equals(forgetPassword.getEmail())){
                return ResponseEntity.badRequest().body("User not found!");
            }
            Account account = accounts.get();
            // Generate and Save Token
            String resetToken = TokenGenerator.generateToken();
            account.setPasswordResetToken(resetToken);
            account.setPasswordResetTokenExpiration(LocalDateTime.now().plusMinutes(30));
            accountRepository.save(account);

            // Send token via email
            sendResetTokenToEmail(account.getEmail(),resetToken);

            return ResponseEntity.ok("Token sent to your email!");
        }

        public ResponseEntity<?> resetPassword(ResetPasswordRequest resetPasswordRequest, String token) {
            Optional<Account> account = accountRepository.findByPasswordResetToken(token);

            if (!account.isPresent()) {
                return ResponseEntity.badRequest().body("Invalid or expired reset token.");
            }

            Account userAccount = account.get();

            // Check if the token is expired
            if (userAccount.getPasswordResetTokenExpiration().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body("Reset token has expired.");
            }

            // Update the password
            userAccount.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
            userAccount.setPasswordResetToken(null); // Clear the token
            userAccount.setPasswordResetTokenExpiration(null); // Clear the expiry
            accountRepository.save(userAccount);

            return ResponseEntity.ok("Password has been reset successfully!");
        }

        public void saveSession(String userName, String token) {
            Optional<Account> accounts = accountRepository.findByUserName(userName);
            Account account = accounts.get();
            Session session = new Session();
            session.setToken(token);
            session.setAccount(account);
            session.setCreatedAt(LocalDateTime.now());
            session.setExpiresAt(LocalDateTime.now().plusHours(10));
            sessionRepository.save(session);
        }

        public Account getAccount(Authentication authentication) {
            String userName = authentication.getName();
            Optional<Account> accounts = accountRepository.findByUserName(userName);
            if(!accounts.isPresent()){
                throw new UsernameNotFoundException("User not found!");
            }
            return accounts.get();
        }

        public ResponseEntity<?> profileView(Authentication authentication) {
            Account account = getAccount(authentication);
            AccountProfile accountProfile = new AccountProfile();
            accountProfile.setFirstName(account.getFirstName());
            accountProfile.setLastName(account.getLastName());
            accountProfile.setEmail(account.getEmail());

            return ResponseEntity.ok(accountProfile);
        }

        public ResponseEntity<?> profileUpdate(ProfileUpdate profileUpdate, Authentication authentication) {
            Account account = getAccount(authentication);

            if (account.getFirstName().equals(profileUpdate.getFirstName()) &&
                account.getLastName().equals(profileUpdate.getLastName()) &&
                account.getEmail().equals(profileUpdate.getEmail())) {
                return ResponseEntity.badRequest().body("No changes detected!");
            }

            if (!account.getEmail().equals(profileUpdate.getEmail()) &&
                accountRepository.existsByEmail(profileUpdate.getEmail())) {
                return ResponseEntity.badRequest().body("Email is already in use!");
            }

            if (!passwordEncoder.matches(profileUpdate.getPassword(), account.getPassword())) {
                return ResponseEntity.badRequest().body("Invalid password!");
            }

            account.setFirstName(profileUpdate.getFirstName());
            account.setLastName(profileUpdate.getLastName());
            account.setEmail(profileUpdate.getEmail());
            accountRepository.save(account);
        
            return ResponseEntity.ok("Profile updated successfully!");
        }
        

        public ResponseEntity<?> confirmPasswordResetToken(ConfirmTokenRequest confirmTokenRequest) {
            String resetToken = confirmTokenRequest.getToken();
            Optional<Account> accounts = accountRepository.findByPasswordResetToken(resetToken);
            if(!accounts.isPresent()){
                return ResponseEntity.badRequest().body("Invalid or expired token.");
            }
            return ResponseEntity.ok("Token is valid!");
        }

        public ResponseEntity<?> getUserInfo(Authentication authentication) {
            Optional<Account> accounts = accountRepository.findByUserName(authentication.getName());
            if (!accounts.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "User not found!"));
            }

            // Kullanıcı adını JSON formatında döndür
            return ResponseEntity.ok(Map.of("userName", accounts.get().getUserName()));
    }

        public ResponseEntity<?> getNameSurname(Authentication authentication) {
            Optional<Account> accounts = accountRepository.findByUserName(authentication.getName());
            if (!accounts.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "User not found!"));
            }

            // Kullanıcı adını JSON formatında döndür
            return ResponseEntity.ok(Map.of("firstName", accounts.get().getFirstName(), "lastName", accounts.get().getLastName()));
        }

        public ResponseEntity<?> changePassword(ChangePasswordDTO changePasswordDTO, Authentication authentication) {
           Account account = getAccount(authentication);

            if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), account.getPassword())) {
                return ResponseEntity.badRequest().body("Invalid old password!");
            }
            if(passwordEncoder.matches(changePasswordDTO.getNewPassword(), account.getPassword())) {
                return ResponseEntity.badRequest().body("New password must be different from the old password!");
            }

            account.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
            accountRepository.save(account);

            return ResponseEntity.ok("Password changed successfully!");
        }
}
