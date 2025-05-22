package notfound.ballog.domain.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notfound.ballog.common.jwt.JwtTokenProvider;
import notfound.ballog.common.response.BaseResponseStatus;
import notfound.ballog.domain.auth.dto.JwtTokenDto;
import notfound.ballog.domain.auth.entity.Auth;
import notfound.ballog.domain.auth.repository.AuthRepository;
import notfound.ballog.domain.auth.request.LoginRequest;
import notfound.ballog.domain.auth.request.SignUpRequest;
import notfound.ballog.domain.auth.response.CheckEmailResponse;
import notfound.ballog.domain.auth.response.LoginResponse;
import notfound.ballog.domain.auth.response.TokenRefreshResponse;
import notfound.ballog.domain.user.dto.UserIdDto;
import notfound.ballog.domain.user.entity.User;
import notfound.ballog.domain.user.service.UserService;
import notfound.ballog.exception.DuplicateDataException;
import notfound.ballog.exception.ValidationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final CustomUserDetailsService customUserDetailsService;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    private final AuthRepository authRepository;

    private final UserService userService;

    @Transactional
    public void signUp(SignUpRequest request){
        String email = request.getEmail();

        String password = passwordEncoder.encode(request.getPassword());

        // 이메일로 유저 조회 -> 탈퇴한 사용자면 복구, 이미 있는 사용자는 예외 처리
        Optional<Auth> existingAuth = authRepository.findByEmail(email);
        if (existingAuth.isPresent()) {
            Auth auth = existingAuth.get();

            // 탈퇴한 사용자라면 복구하고 저장
            if (!auth.getIsActive()) {
                User user = auth.getUser();

                user.reactivate(request.getNickname(), request.getBirthDate(), request.getProfileImageUrl());

                User savedUser = userService.reactivateUser(user);

                auth.reactivate(savedUser, email, password);

                authRepository.save(auth);
            } else {
                throw new DuplicateDataException(BaseResponseStatus.DUPLICATE_EMAIL);
            }
        } else {
            // 1. 신규 유저 생성
            User newUser = request.toUserEntity(request);

            User savedUser = userService.signUp(newUser);

            // 2. 신규 Auth 생성
            Auth newAuth = request.toAuthEntity(savedUser, email, password);

            authRepository.save(newAuth);
        }
    }

    @Transactional
    public LoginResponse login(LoginRequest request){
        String email = request.getEmail();

        String password = request.getPassword();

        // 1. 이메일로 auth 조회 -> CustomUserDetails 생성
        CustomUserDetails customUserDetails = customUserDetailsService.loadUserByEmail(email);

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(password, customUserDetails.getPassword())) {
            throw new ValidationException(BaseResponseStatus.PASSWORD_MISMATCH);
        }

        // 3. 기본 인증 토큰 생성 -> JWT 토큰 생성
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(null, null, customUserDetails.getAuthorities());

        authentication.setDetails(customUserDetails);

        JwtTokenDto token = jwtTokenProvider.generateToken(authentication);

        // 4. refreshToken 저장
        Auth auth = customUserDetails.getAuth();

        auth.changeRefreshToken(token.getRefreshToken());

        Auth savedAuth = authRepository.save(auth);

        UserIdDto userIdDto = UserIdDto.of(savedAuth);

        return LoginResponse.of(token, userIdDto);
    }

    @Transactional
    public void logOut(UUID userId) {
        // DB에 저장된 refresh 토큰 삭제
        Auth auth = authRepository.findByUser_UserIdAndIsActiveTrue(userId)
                        .orElseThrow(() -> new ValidationException(BaseResponseStatus.USER_NOT_FOUND));

        auth.changeRefreshToken(null);

        authRepository.save(auth);
    }

    @Transactional
    public TokenRefreshResponse refreshToken(UUID userId, String refreshToken) {
        Auth auth = authRepository.findByUser_UserIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new ValidationException(BaseResponseStatus.USER_NOT_FOUND));

        // 1. 토큰 만료 체크
        jwtTokenProvider.validateToken(refreshToken);

        // 2. db 토큰과 비교
        if (!auth.getRefreshToken().equals(refreshToken)) {
            throw new ValidationException(BaseResponseStatus.INVALID_TOKEN);
        }

        // 3. 탈퇴한 사용자인지 확인
        if (!auth.getIsActive()) {
            throw new ValidationException(BaseResponseStatus.USER_INACTIVE);
        }

        // 4. 토큰 재발급
        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);

        JwtTokenDto token = jwtTokenProvider.generateToken(authentication);

        auth.changeRefreshToken(token.getRefreshToken());

        authRepository.save(auth);

        return TokenRefreshResponse.of(token);
    }

    @Transactional
    public CheckEmailResponse checkEmail(String email) {
        boolean isExist = authRepository.existsByEmailAndIsActiveTrue(email);

        return CheckEmailResponse.of(!isExist);
    }

    @Transactional
    public void signOut(UUID userId) {
        Auth auth = authRepository.findByUser_UserIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new ValidationException(BaseResponseStatus.USER_NOT_FOUND));

        auth.changeIsActive(false);

        auth.changeRefreshToken(null);

        authRepository.save(auth);
    }
}
