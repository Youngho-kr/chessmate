package com.chessmate.chess_server.domain.user;

import com.chessmate.chess_server.domain.user.dto.*;
import com.chessmate.chess_server.global.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.swing.text.html.Option;
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

        TokenResponse response = userService.login(request);

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

    @Test
    @DisplayName("Access Token 재발급")
    void reissue() {

        String email = "test@test.com";
        String refreshToken = jwtProvider.generateRefreshToken(email);

        ReissueRequest request = new ReissueRequest();
        request.setRefreshToken(refreshToken);

        when(refreshTokenRepository.find(email)).thenReturn(refreshToken);

        TokenResponse response = userService.reissue(request);

        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
    }

    @Test
    @DisplayName("유효하지 않은 Refresh Token으로 재발급 시 예외 발생")
    void reissue_invalidToken() {

        ReissueRequest request = new ReissueRequest();
        request.setRefreshToken("invalid.token.value");

        assertThatThrownBy(() -> userService.reissue(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 Refresh Token 입니다.");
    }

    @Test
    @DisplayName("저장된 Refresh Token과 일치하지 않으면 예외 발생")
    void reissue_tokenMismatch() {

        String email = "test@test.com";
        String refreshToken = jwtProvider.generateRefreshToken(email);

        ReissueRequest request = new ReissueRequest();
        request.setRefreshToken(refreshToken);

        when(refreshTokenRepository.find(email)).thenReturn("different.token");

        assertThatThrownBy(() -> userService.reissue(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh Token이 일치하지 않습니다.");
    }

    @Test
    @DisplayName("프로필 조회")
    void getProfile() {
        String email = "test@test.com";
        String nickname = "user";
        User user = new User(email, passwordEncoder.encode("password123"), nickname);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserProfileResponse response = userService.getProfile(email);

        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getNickname()).isEqualTo(nickname);
    }

    @Test
    @DisplayName("존재하지 않는 프로필 조회 시 예외 발생")
    void getProfile_notFound() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile("notfound@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원입니다.");
    }

    private SignupRequest createRequest(String email, String password, String nickname) {
        SignupRequest request = new SignupRequest();

        request.setEmail(email);
        request.setPassword(password);
        request.setNickname(nickname);

        return request;
    }
}