package com.chessmate.chess_server.domain.user;

import com.chessmate.chess_server.domain.user.dto.*;
import com.chessmate.chess_server.global.jwt.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getNickname()
        );

        userRepository.save(user);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        refreshTokenRepository.save(user.getEmail(), refreshToken, jwtProvider.getRefreshExpiration());

        return new TokenResponse(accessToken, refreshToken);
    }

    public void logout(String email) {
        refreshTokenRepository.delete(email);
    }

    /*
     * Access Token 재발급
     */
    public TokenResponse reissue(ReissueRequest request) {

        String refreshToken = request.getRefreshToken();

        if (!jwtProvider.isValid(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token 입니다.");
        }

        String email = jwtProvider.getEmail(refreshToken);

        String savedToken = refreshTokenRepository.find(email);
        if (!refreshToken.equals(savedToken)) {
            throw new IllegalArgumentException("Refresh Token이 일치하지 않습니다.");
        }

        String newAccessToken = jwtProvider.generateAccessToken(email);
        String newRefreshToken = jwtProvider.generateRefreshToken(email);

        refreshTokenRepository.save(email, newRefreshToken, jwtProvider.getRefreshExpiration());

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    /*
     * 사용자 프로필 조회
     */
    public UserProfileResponse getProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->  new IllegalArgumentException("존재하지 않는 회원입니다."));

        return new UserProfileResponse(user);
    }
}
