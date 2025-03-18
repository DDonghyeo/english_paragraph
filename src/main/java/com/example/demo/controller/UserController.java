package com.example.demo.controller;

import com.example.demo.annotation.CurrentUser;
import com.example.demo.auth.AuthUser;
import com.example.demo.dto.request.JwtDto;
import com.example.demo.dto.request.LoginRequestDto;
import com.example.demo.dto.request.UserRequest;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/user")
@RestController
public class UserController {

    private final UserService userService;

    @Operation(tags = "user", summary = "사용자 조회", description = "사용자 정보 조회")
    @GetMapping("")
    public ResponseEntity<?> getUser(@CurrentUser AuthUser authUser) {
        return ResponseEntity.ok(userService.getUser(authUser));
    }

    @Operation(tags = "user", summary = "사용자 회원 가입", description = "사용자 회원 가입")
    @PostMapping("/signup")
    public ResponseEntity<?> createUser(UserRequest.SignUpDto signUpDto) {
        userService.signup(signUpDto);
        return ResponseEntity.ok(null);
    }


    //Swagger용 가짜 컨트롤러
    @Operation(tags = "user", summary = "사용자 로그인", description = "로그인")
    @PostMapping("/login")
    public ResponseEntity<JwtDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        return null;
    }
}
