package com.bookworm.auth;

import com.bookworm.api.model.AuthResponse;
import com.bookworm.api.model.LoginRequest;
import com.bookworm.api.model.RefreshRequest;
import com.bookworm.api.model.RegisterRequest;
import com.bookworm.cart.CartService;
import com.bookworm.common.ApiException;
import com.bookworm.member.UserMapper;
import com.bookworm.member.entity.UserEntity;
import com.bookworm.member.entity.UserRole;
import com.bookworm.member.repo.UserRepository;
import com.bookworm.wishlist.WishlistService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final CartService cartService;
    private final WishlistService wishlistService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.getEmail())) {
            throw ApiException.conflict(com.bookworm.api.model.ApiErrorCode.CONFLICT,
                    "An account with this email already exists");
        }

        UserEntity user = UserEntity.builder()
                .email(req.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .phone(req.getPhone())
                .role(UserRole.CUSTOMER)
                .giftPoints(0)
                .build();
        user = userRepository.save(user);

        // Provision empty cart + wishlist rows so later reads never have to lazily create them.
        cartService.getOrCreateCart(user.getId());
        wishlistService.getOrCreateWishlist(user.getId());

        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        UserEntity user = userRepository.findByEmailIgnoreCase(req.getEmail())
                .orElseThrow(() -> ApiException.unauthenticated("Invalid email or password"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw ApiException.unauthenticated("Invalid email or password");
        }
        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshRequest req) {
        Claims claims = jwtService.parseRefreshToken(req.getRefreshToken());
        Long userId = Long.valueOf(claims.getSubject());
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthenticated("User no longer exists"));
        return issueTokens(user);
    }

    // ---------------------------------------------------------------------------------------

    private AuthResponse issueTokens(UserEntity user) {
        String access = jwtService.issueAccessToken(user);
        String refresh = jwtService.issueRefreshToken(user);
        return new AuthResponse()
                .accessToken(access)
                .refreshToken(refresh)
                .expiresInSeconds((int) jwtService.accessTtlSeconds())
                .user(userMapper.toDto(user));
    }
}
