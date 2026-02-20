package com.zest.product.service;

import com.zest.product.dto.AuthResponse;
import com.zest.product.dto.LoginRequest;
import com.zest.product.entity.RefreshToken;
import com.zest.product.entity.User;
import com.zest.product.repository.RefreshTokenRepository;
import com.zest.product.repository.UserRepository;
import com.zest.product.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        if (!encoder.matches(request.getPassword(), user.getPassword()))
            throw new RuntimeException("Invalid credentials");

        String accessToken = jwtUtil.generateToken(user);

        RefreshToken refresh = createRefreshToken(user);

        return new AuthResponse(accessToken, refresh.getToken());
    }

    public AuthResponse refresh(String token) {

        RefreshToken refreshToken = refreshRepository.findByToken(token)
                .orElseThrow();

        if (refreshToken.isRevoked() || refreshToken.getExpiryDate().isBefore(Instant.now()))
            throw new RuntimeException("Invalid refresh token");

        refreshToken.setRevoked(true);
        refreshRepository.save(refreshToken);

        String newAccess = jwtUtil.generateToken(refreshToken.getUser());
        RefreshToken newRefresh = createRefreshToken(refreshToken.getUser());

        return new AuthResponse(newAccess, newRefresh.getToken());
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
        return refreshRepository.save(token);
    }
}
