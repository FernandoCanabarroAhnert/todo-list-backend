package com.test.todo_list_backend.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;

import com.test.todo_list_backend.factories.RoleFactory;
import com.test.todo_list_backend.factories.UserFactory;
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
import com.test.todo_list_backend.services.exceptions.AccountNotActivatedException;
import com.test.todo_list_backend.services.exceptions.AlreadyExistingEmailException;
import com.test.todo_list_backend.services.exceptions.ExpiredActivationCodeException;
import com.test.todo_list_backend.services.exceptions.ForbiddenException;
import com.test.todo_list_backend.services.exceptions.IncorrectCurrentPasswordException;
import com.test.todo_list_backend.services.exceptions.ResourceNotFoundException;
import com.test.todo_list_backend.services.exceptions.UnauthorizedException;
import com.test.todo_list_backend.services.impl.UserServiceImpl;
import com.test.todo_list_backend.utils.ObtainUserEmailFromJWT;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ActivationCodeRepository activationCodeRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtEncoder jwtEncoder;
    @Mock
    private JwtDecoder jwtDecoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private ObtainUserEmailFromJWT obtainUserEmailFromJWT;

    private User user;
    private Role role;
    private RegistrationRequestDTO registrationRequest;
    private LoginRequestDTO loginRequest;
    private Jwt jwt;
    private Authentication authentication;
    private ActivationCode activationCode;
    private AccountActivationRequestDTO accountActivationRequest;
    private UpdatePasswordRequestDTO updatePasswordRequest;
    private UpdateUserInfosRequestDTO updateUserInfosRequest;

    @BeforeEach
    public void setup() {
        this.user = UserFactory.create();
        this.role = RoleFactory.create();
        this.registrationRequest = UserFactory.createRegistrationRequest();
        this.loginRequest = UserFactory.createLoginRequest();
        this.activationCode = UserFactory.createActivationCode();
        this.accountActivationRequest = new AccountActivationRequestDTO("code");
        this.updatePasswordRequest = UserFactory.createUpdatePasswordRequest();
        this.updateUserInfosRequest = UserFactory.createUpdateUserInfosRequest();

        this.jwt = Jwt.withTokenValue("token")
            .headers(headers -> {
                headers.put("alg", "HS256");
                headers.put("typ", "JWT");
            })
            .claim("username", user.getEmail())
            .build();
        this.authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
    }

    @Test
    public void registerShouldThrowNoExceptionWhenEmailDoesNotExists() {
        when(userRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByAuthority("ROLE_USER")).thenReturn(role);
        when(userRepository.save(any(User.class))).thenReturn(user);

        assertThatCode(() -> userService.register(registrationRequest)).doesNotThrowAnyException();
    }

    @Test
    public void registerShouldThrowAlreadyExistingEmailExceptionWhenEmailExists() {
        when(userRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.register(registrationRequest)).isInstanceOf(AlreadyExistingEmailException.class);
    }

    @Test
    public void loginShouldReturnLoginResponseDTOWhenDataIsValid() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        LoginResponseDTO response = userService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo(jwt.getTokenValue());

    }

    @Test
    public void loginShouldThrowUsernameNotFoundExceptionWhenEmailDoesNotExist() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(loginRequest)).isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    public void loginShouldThrowAccountNotActivatedExceptionWhenAccountIsNotActivated() {
        user.setActive(false);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.login(loginRequest)).isInstanceOf(AccountNotActivatedException.class);
    }

    @Test
    public void activateAccountShouldActivateUserAndThrowNoExceptionWhenCodeIsValid() {
        when(activationCodeRepository.findByCode("code")).thenReturn(Optional.of(activationCode));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(activationCodeRepository.save(any(ActivationCode.class))).thenReturn(activationCode);

        assertThatCode(() -> userService.activateAccount(accountActivationRequest)).doesNotThrowAnyException();
    }

    @Test
    public void activateAccountShouldThrowResourceNotFoundExceptionWhenCodeDoesNotExist() {
        when(activationCodeRepository.findByCode("code")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.activateAccount(accountActivationRequest)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void activateAccountShouldThrowExpiredActivationCodeExceptionWhenActivationCodeIsInvalid() {
        this.activationCode.setExpiresAt(LocalDateTime.now().minusMinutes(30L));
        when(activationCodeRepository.findByCode("code")).thenReturn(Optional.of(activationCode));

        assertThatThrownBy(() -> userService.activateAccount(accountActivationRequest)).isInstanceOf(ExpiredActivationCodeException.class);
    }

    @Test
    public void verifyTokenShouldThrowNoExceptionWhenTokenIsValid() {
        when(jwtDecoder.decode("token")).thenReturn(jwt);

        assertThatCode(() -> userService.verifyToken("token")).doesNotThrowAnyException();
    }

    @Test
    public void verifyTokenShouldThrowUnauthorizedExceptionWhenTokenIsNull() {
        assertThatThrownBy(() -> userService.verifyToken(null)).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    public void verifyTokenShouldThrowUnauthorizedExceptionWhenTokenIsEmpty() {
        assertThatThrownBy(() -> userService.verifyToken("")).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    public void verifyTokenShouldThrowForbiddenExceptionWhenTokenIsInvalid() {
        when(jwtDecoder.decode("token")).thenThrow(new JwtException(""));

        assertThatThrownBy(() -> userService.verifyToken("token")).isInstanceOf(ForbiddenException.class);
    }

    @Test
    public void getConnectedUserShouldReturnUserWhenUserExists() {
        when(obtainUserEmailFromJWT.getUserEmail()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatCode(() -> userService.getConnectedUser()).doesNotThrowAnyException();
        assertThat(user.getId()).isEqualTo(user.getId());
        assertThat(user.getFullName()).isEqualTo(user.getFullName());
        assertThat(user.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    public void getConnectedUserShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
        when(obtainUserEmailFromJWT.getUserEmail()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getConnectedUser()).isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    public void verifyExistingEmailShouldReturnTrueWhenEmailExists() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        VerifyExistingEmailResponseDTO response = userService.verifyExistingEmail(user.getEmail());

        assertThat(response).isNotNull();
        assertThat(response.isAlreadyInUse()).isTrue();
    }

    @Test
    public void verifyExistingEmailShouldReturnFalseWhenEmailDoesNotExist() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        VerifyExistingEmailResponseDTO response = userService.verifyExistingEmail(user.getEmail());

        assertThat(response).isNotNull();
        assertThat(response.isAlreadyInUse()).isFalse();
    }

    @Test
    public void updateUserInfosShouldThrowNoExceptionWhenEmailDoesNotExist() {
        UserServiceImpl spy = spy(this.userService);
        when(obtainUserEmailFromJWT.getUserEmail()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(spy.getConnectedUser()).thenReturn(user);
        when(userRepository.findByEmail(updateUserInfosRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        assertThatCode(() -> spy.updateUserInfos(updateUserInfosRequest)).doesNotThrowAnyException();
    }

    @Test
    public void updateUserInfosShouldThrowNoExceptionWhenEmailExistsButIsFromCurrentLoggedUser() {
        UserServiceImpl spy = spy(this.userService);
        doReturn(user).when(spy).getConnectedUser();
        when(userRepository.findByEmail(updateUserInfosRequest.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        assertThatCode(() -> spy.updateUserInfos(updateUserInfosRequest)).doesNotThrowAnyException();
    }

    @Test
    public void updateUserInfosShouldThrowAlreadyExistingEmailExceptionWhenEmailExistsAndIsNotFromCurrentLoggedUser() {
        User otherUser = new User();
        otherUser.setId("other-id");

        UserServiceImpl spy = spy(userService);
        doReturn(user).when(spy).getConnectedUser();
        when(userRepository.findByEmail(updateUserInfosRequest.getEmail())).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> spy.updateUserInfos(updateUserInfosRequest)).isInstanceOf(AlreadyExistingEmailException.class);
    }

    @Test
    public void updatePasswordShouldThrowNoExceptionWhenCurrentPasswordIsCorrect() {
        UserServiceImpl spy = spy(userService);
        doReturn(user).when(spy).getConnectedUser();
        when(passwordEncoder.matches(updatePasswordRequest.getCurrentPassword(), user.getPassword())).thenReturn(true);

        assertThatCode(() -> spy.updatePassword(updatePasswordRequest)).doesNotThrowAnyException();
    }

    @Test
    public void updatePasswordShouldThrowIncorrectCurrentPasswordExceptionWhenCurrentPasswordIsIncorrect() {
        UserServiceImpl spy = spy(userService);
        doReturn(user).when(spy).getConnectedUser();
        when(passwordEncoder.matches(updatePasswordRequest.getCurrentPassword(), user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> spy.updatePassword(updatePasswordRequest)).isInstanceOf(IncorrectCurrentPasswordException.class);
    }


}
