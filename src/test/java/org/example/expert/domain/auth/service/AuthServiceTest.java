package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;


    @Test
    void signupTest(){
        // given
        SignupRequest signupRequest = new SignupRequest("duduio2050@gmail.com", "qwer123", "ADMIN");

        String encodePassword = passwordEncoder.encode(signupRequest.getPassword());

        User newUser = new User(
                signupRequest.getEmail(),
                encodePassword,
                UserRole.of(signupRequest.getUserRole())
        );
        String bearerToken = jwtUtil.createToken(newUser.getId(), newUser.getEmail(), UserRole.of(signupRequest.getUserRole()));

        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(newUser);
        given(jwtUtil.createToken(newUser.getId(), newUser.getEmail(), UserRole.of(signupRequest.getUserRole()))).willReturn(bearerToken);

        // when
        SignupResponse signupResponse = authService.signup(signupRequest);

        // then
        assertEquals(bearerToken, signupResponse.getBearerToken());
    }

    @Test
    void signupInvalidTest(){

        SignupRequest signupRequest = new SignupRequest("duduio2050@gmail.com", "qwer123", "ADMIN");

        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(true);

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authService.signup(signupRequest);
        });

//        System.out.println(exception);

        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
    }


    @Test
    void signinTest(){

        // given
        User mockUser = new User("duduio2050@gmail.com", "qwer123", UserRole.ADMIN);

        SigninRequest signinRequest = new SigninRequest("duduio2050@gmail.com", "qwer123");

        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.of(mockUser));

        given(passwordEncoder.matches(signinRequest.getPassword(), mockUser.getPassword())).willReturn(true);

        String bearerToken = jwtUtil.createToken(mockUser.getId(), mockUser.getEmail(), mockUser.getUserRole());

        // when

        SigninResponse signinResponse = authService.signin(signinRequest);


        assertEquals(signinResponse.getBearerToken(), bearerToken);


    }

    @Test
    void signinInvalidTest(){

        User mockUser = new User("duduio2050@gmail.com", "qwer123", UserRole.ADMIN);

        SigninRequest signinRequest = new SigninRequest("duduio2050@gmail.com", "qwer123");

        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.empty());

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authService.signin(signinRequest);
        });

        assertEquals("가입되지 않은 유저입니다.", exception.getMessage());

    }

    @Test
    void signinAuthExceptionTest(){

        User mockUser = new User("duduio2050@gmail.com", "qwer123", UserRole.ADMIN);

        SigninRequest signinRequest = new SigninRequest("duduio2050@gmail.com", "qwer123");

        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.of(mockUser));

        given(passwordEncoder.matches(signinRequest.getPassword(), mockUser.getPassword())).willReturn(false);

        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.signin(signinRequest);
        });

        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());

    }


}