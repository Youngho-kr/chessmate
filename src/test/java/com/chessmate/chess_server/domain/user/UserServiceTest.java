package com.chessmate.chess_server.domain.user;

import com.chessmate.chess_server.domain.user.dto.SignupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void beforeEach() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository, new BCryptPasswordEncoder());
    }

    @Test
    void signup() {
        SignupRequest request = createRequest("test@test.com", "password123", "user");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByNickname(request.getNickname())).thenReturn(false);

        userService.signup(request);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void signup_duplicateEmail() {
        SignupRequest request = createRequest("test@test.com", "password123", "user");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");
    }

    @Test
    void signup_duplicateNickname() {
        SignupRequest request = createRequest("test@test.com", "password123", "user");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByNickname(request.getNickname())).thenReturn(true);

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 닉네임입니다.");
    }

    private SignupRequest createRequest(String email, String password, String nickname) {
        SignupRequest request = new SignupRequest();

        request.setEmail(email);
        request.setPassword(password);
        request.setNickname(nickname);

        return request;
    }
}