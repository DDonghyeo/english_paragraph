package com.example.demo.service;

import com.example.demo.auth.AuthUser;
import com.example.demo.dto.request.UserRequest;
import com.example.demo.dto.response.UserResponseDto;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(UserRequest.SignUpDto signUpDto) {

        String email = signUpDto.email();
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException();
        }
        userRepository.save(signUpDto.toEntity(passwordEncoder));
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUser(AuthUser authUser) {
        return UserResponseDto.from(
                userRepository.findByEmail(authUser.getEmail()).orElseThrow()
        );
    }
//
//    @Transactional
//    public void updateUser(AuthUser authUser, UserRequest.UpdateDto updateDto) {
//        User user = userRepository.findByEmail(authUser.getEmail()).orElseThrow(
//                () -> new CustomException(UserErrorCode.USER_NOT_FOUND));
//        user.update(updateDto);
//    }
//
    @Transactional
    public void deleteUser(AuthUser authUser) {
        User user = userRepository.findByEmail(authUser.getEmail()).orElseThrow(
                () -> new RuntimeException());
        //soft delete
        userRepository.delete(user);
    }
}
