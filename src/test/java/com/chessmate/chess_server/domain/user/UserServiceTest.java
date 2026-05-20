package com.chessmate.chess_server.domain.user;

import com.chessmate.chess_server.domain.user.dto.LoginRequest;
import com.chessmate.chess_server.domain.user.dto.LoginResponse;
import com.chessmate.chess_server.domain.user.dto.SignupRequest;
import com.chessmate.chess_server.global.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;
    private JwtProvider jwtProvider;

    @BeforeEach
    void beforeEach() {
        userRepository = mock(UserRepository.class);
        jwtProvider = new JwtProvider(
                "test-secret-key-minimum-32-characters-long",
                1800000L,
                604800000L
        );
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(
                userRepository,
                passwordEncoder,
                jwtProvider,
                refreshTokenRepository
        );
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

    @Test
    void login() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password123");

        User user = new User(
                "test@test.com",
                passwordEncoder.encode("password123"),
                "user"
        );

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        LoginResponse response = userService.login(request);

        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
    }

    @Test
    void login_emailNotFound() {
        LoginRequest request = new LoginRequest();

        request.setEmail("notfound@test.com");
        request.setPassword("password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    void login_wrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("wrongPassword");

        User user = new User("test@test.com", passwordEncoder.encode("password123"), "testuser");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    private SignupRequest createRequest(String email, String password, String nickname) {
        SignupRequest request = new SignupRequest();

        request.setEmail(email);
        request.setPassword(password);
        request.setNickname(nickname);

        return request;
    }
}