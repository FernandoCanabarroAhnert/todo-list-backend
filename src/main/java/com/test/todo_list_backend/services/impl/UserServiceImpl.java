package com.test.todo_list_backend.services.impl;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendgrid.helpers.mail.Mail;
import com.test.todo_list_backend.mappers.UserMapper;
import com.test.todo_list_backend.models.dtos.AccountActivationRequestDTO;
import com.test.todo_list_backend.models.dtos.LoginRequestDTO;
import com.test.todo_list_backend.models.dtos.LoginResponseDTO;
import com.test.todo_list_backend.models.dtos.RegistrationRequestDTO;
import com.test.todo_list_backend.models.dtos.UpdatePasswordRequestDTO;
import com.test.todo_list_backend.models.dtos.UpdateUserInfosRequestDTO;
import com.test.todo_list_backend.models.dtos.VerifyExistingEmailResponseDTO;
import com.test.todo_list_backend.models.entities.ActivationCode;
import com.test.todo_list_backend.models.entities.Role;
import com.test.todo_list_backend.models.entities.User;
import com.test.todo_list_backend.repositories.ActivationCodeRepository;
import com.test.todo_list_backend.repositories.RoleRepository;
import com.test.todo_list_backend.repositories.UserRepository;
import com.test.todo_list_backend.services.EmailService;
import com.test.todo_list_backend.services.UserService;
import com.test.todo_list_backend.services.exceptions.AccountNotActivatedException;
import com.test.todo_list_backend.services.exceptions.AlreadyExistingEmailException;
import com.test.todo_list_backend.services.exceptions.ExpiredActivationCodeException;
import com.test.todo_list_backend.services.exceptions.ForbiddenException;
import com.test.todo_list_backend.services.exceptions.IncorrectCurrentPasswordException;
import com.test.todo_list_backend.services.exceptions.ResourceNotFoundException;
import com.test.todo_list_backend.services.exceptions.UnauthorizedException;
import com.test.todo_list_backend.utils.ObtainUserEmailFromJWT;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ActivationCodeRepository activationCodeRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final AuthenticationManager authenticationManager;
    private final ObtainUserEmailFromJWT obtainUserEmailFromJWT;

    private final long SECONDS_IN_A_DAY = 86400L;

    @Override
    @Transactional
    public void register(RegistrationRequestDTO request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new AlreadyExistingEmailException("Email already exists");
        }
        User user = UserMapper.convertRequestDTOToEntity(request, this.passwordEncoder);
        Role role = roleRepository.findByAuthority("ROLE_USER");
        user.addRole(role);
        user = userRepository.save(user);
        this.sendConfirmationEmail(user);
    }

    @Override
    @Transactional
    public void sendConfirmationEmail(User user) {
        String code = this.generateAndSaveActivationCode(user);
        Mail mail = emailService.createEmailTemplate(user.getEmail(), user.getFullName(), code, "Ativação de Conta");
        emailService.sendEmail(mail);
    }

    @Override
    @Transactional
    public String generateAndSaveActivationCode(User user) {
        String code = this.generateCode();
        ActivationCode activationCode = ActivationCode.builder()
            .code(code)
            .user(user)
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusMinutes(30L))
            .isValidated(false)
            .validatedAt(null)
            .build();
        activationCodeRepository.save(activationCode);
        return code;
    }

    @Override
    public String generateCode() {
        String chars = "0123456789";
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder();
        int charsLength = chars.length() - 1;
        for (int i = 0; i < charsLength; i++) {
            int randomIndex = secureRandom.nextInt(charsLength);
            stringBuilder.append(chars.charAt(randomIndex));
        }
        return stringBuilder.toString();
    }

    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException("User with username " + request.getEmail() + " not found"));
        if (!user.isActive()) {
            throw new AccountNotActivatedException("Account not activated");
        }
        Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(request.getEmail(), request.getPassword());
        Authentication response = authenticationManager.authenticate(authentication);
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("todo-list-app-auth")
            .subject(response.getName())
            .claim("username", response.getName())
            .claim("nickname", user.getUserName())
            .claim("fullName", user.getFullName())
            .claim("authorities", response.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(SECONDS_IN_A_DAY))
            .build();
        String jwt = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new LoginResponseDTO(jwt, SECONDS_IN_A_DAY);
    }

    @Override
    @Transactional
    public void activateAccount(AccountActivationRequestDTO request) {
        ActivationCode activationCode = this.activationCodeRepository.findByCode(request.getCode())
            .orElseThrow(() -> new ResourceNotFoundException("Activation Code", request.getCode()));
        User user = activationCode.getUser();
        if (!activationCode.isValid()) {
            this.sendConfirmationEmail(user);
            throw new ExpiredActivationCodeException(user.getEmail());
        }
        user.setActive(true);
        userRepository.save(user);
        activationCode.setValidated(true);
        activationCode.setValidatedAt(LocalDateTime.now());
        activationCodeRepository.save(activationCode);
    }

    @Override
    @Transactional
    public void verifyToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                throw new UnauthorizedException("No Token provided");
            }
            this.jwtDecoder.decode(token);
        }
        catch (JwtException exception) {
            throw new ForbiddenException("Invalid Token");
        }
    }

    @Override
    @Transactional
    public User getConnectedUser() {
        String email = obtainUserEmailFromJWT.getUserEmail();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User with username " + email + " not found"));
        return user;
    }

    @Override
    @Transactional
    public VerifyExistingEmailResponseDTO verifyExistingEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return new VerifyExistingEmailResponseDTO(user.isPresent());
    }

    @Override
    @Transactional
    public void updateUserInfos(UpdateUserInfosRequestDTO request) {
        User user = this.getConnectedUser();
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
            throw new AlreadyExistingEmailException("Email already exists");
        }
        UserMapper.updateUserEntity(user, request);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updatePassword(UpdatePasswordRequestDTO request) {
        User user = this.getConnectedUser();
        String currentPassword = request.getCurrentPassword();
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IncorrectCurrentPasswordException();
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

}
