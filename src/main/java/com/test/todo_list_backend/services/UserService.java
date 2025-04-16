package com.test.todo_list_backend.services;

import com.test.todo_list_backend.models.dtos.AccountActivationRequestDTO;
import com.test.todo_list_backend.models.dtos.LoginRequestDTO;
import com.test.todo_list_backend.models.dtos.LoginResponseDTO;
import com.test.todo_list_backend.models.dtos.RegistrationRequestDTO;
import com.test.todo_list_backend.models.dtos.UpdatePasswordRequestDTO;
import com.test.todo_list_backend.models.dtos.UpdateUserInfosRequestDTO;
import com.test.todo_list_backend.models.dtos.VerifyExistingEmailResponseDTO;
import com.test.todo_list_backend.models.entities.User;

public interface UserService {

    void register(RegistrationRequestDTO request);
    void sendConfirmationEmail(User user);
    String generateAndSaveActivationCode(User user);
    String generateCode();

    LoginResponseDTO login(LoginRequestDTO request);
    void activateAccount(AccountActivationRequestDTO request);
    void verifyToken(String token);

    User getConnectedUser();
    VerifyExistingEmailResponseDTO verifyExistingEmail(String email);

    void updateUserInfos(UpdateUserInfosRequestDTO request);
    void updatePassword(UpdatePasswordRequestDTO request);
    

}
