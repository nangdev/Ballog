package notfound.ballog.common.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notfound.ballog.common.response.BaseResponseStatus;
import notfound.ballog.domain.auth.dto.JwtTokenDto;
import notfound.ballog.domain.auth.repository.AuthRepository;
import notfound.ballog.domain.auth.service.CustomUserDetails;
import notfound.ballog.domain.auth.service.CustomUserDetailsService;
import notfound.ballog.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final AuthRepository authRepository;

    private Key signingKey;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expire-ms}")
    private Long accessExpireMs;

//    @Value("${jwt.refresh-expire-ms}")        // 리프레쉬 토큰 만료 없음
//    private Long refreshExpireMs;

    private final CustomUserDetailsService customUserDetailsService;

    /** 키 생성 */
    @PostConstruct
    public void init() {
        // Base64로 인코딩된 secret 문자열을 디코딩 → HMAC-SHA256 키 객체 생성
        byte[] keyBytes = Decoders.BASE64.decode(secret);

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /** JWT 토큰 생성 */
    public JwtTokenDto generateToken(Authentication authentication) {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getDetails();

        // 토큰 subject에 담을 authId
        Integer authId = customUserDetails.getAuthId();

        // CustomUserDetails에서 권한 리스트 추출 -> 문자열로 변환
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Long now = System.currentTimeMillis();

        String accessToken = Jwts.builder()
                .setSubject(String.valueOf(authId))
                .setExpiration(new Date((now + accessExpireMs)))    // Access 토큰 만료 시간 설정
                .signWith(signingKey, SignatureAlgorithm.HS256)     // HS256 알고리즘 서명
                .claim("auth", authorities)
                .claim("type", "ACCESS")
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(String.valueOf(authId))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .claim("type", "REFRESH")
                .compact();

        return JwtTokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /** 토큰 파싱(유효성 검사 포함) */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** 토큰 유효성 검사 */
    public void validateToken(String token) {
        try {
            parseToken(token);
        } catch (ExpiredJwtException e) {
            Claims claims = e.getClaims();
            String type = claims.get("type").toString();

            // Refresh 토큰이면 만료 무시
            if (type.equals("REFRESH")) {
                return;
            }
            // Access 토큰이면 만료 예외 처리
            throw new ValidationException(BaseResponseStatus.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            // 서명 위조, 포맷 오류, 기타 jwt 오류
            throw new ValidationException(BaseResponseStatus.INVALID_TOKEN);
        }
    }

    /** 토큰에서 Authentication 추출 */
    public Authentication getAuthentication(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new ValidationException(BaseResponseStatus.INVALID_TOKEN);
        }

        try {
            // 1. 토큰 파싱(=유효성 검사)
            Claims claims = parseToken(token);

            // 2. 토큰에서 authId 가져와서 CustomUserDetails 생성
            Integer authId = Integer.parseInt(claims.getSubject());
            CustomUserDetails customUserDetails = customUserDetailsService.loadUserByAuthId(authId);

            // 3. Authorities 추출 -> Access 토큰은 claims안에, Refresh 토큰은 userDetails에서 가져오기
            Object authClaim = claims.get("auth");
            Collection<? extends GrantedAuthority> authorities;
                // Access 토큰인 경우
            if (authClaim != null) {
                authorities = Arrays.stream(authClaim.toString().split(","))
                                    .map(SimpleGrantedAuthority::new)
                                    .collect(Collectors.toList());

            } else {
                // Refresh 토큰인 경우
                authorities = customUserDetails.getAuthorities();
            }

            // 4. SecurityContext에 저장할 Authentication
            UUID userId = customUserDetails.getUserId();

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            auth.setDetails(customUserDetails);

            return auth;
        } catch (ExpiredJwtException e) {
            throw new ValidationException(BaseResponseStatus.EXPIRED_TOKEN);
        } catch (JwtException e) {
            throw new ValidationException(BaseResponseStatus.INVALID_TOKEN);
        }
    }
}

