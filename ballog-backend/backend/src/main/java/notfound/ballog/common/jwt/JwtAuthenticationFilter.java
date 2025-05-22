package notfound.ballog.common.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import notfound.ballog.common.response.BaseResponse;
import notfound.ballog.common.response.BaseResponseStatus;
import notfound.ballog.exception.ValidationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;

    /** Header에서 토큰 추출 */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        } else {
            throw new ValidationException(BaseResponseStatus.MISSING_TOKEN);
        }
    }

    /** 에러 응답 작성 */
    private void setErrorResponse(HttpServletResponse httpResponse, BaseResponseStatus status) throws IOException {
        httpResponse.setStatus(200);
        httpResponse.setContentType("application/json");
        httpResponse.setCharacterEncoding("UTF-8");

        // body에 BaseResponse 담기
        BaseResponse<Void> body = BaseResponse.error(status);

        String json = new ObjectMapper().writeValueAsString(body);

        httpResponse.getWriter().write(json);
    }

    /** 모든 요청에 실행되는 필터 */
    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
                         throws IOException, ServletException {
        // 1. ServletRequest/Response를 Http 전용 객체로 캐스팅
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 2. 요청된 URI 경로와 Http 메서드 추출
        String path = httpRequest.getServletPath();
        String method = httpRequest.getMethod();

        // 3. 인증 없이 허용할 경로 목록
        if ((path.equals("/v1/auth/signup")) ||             // 회원가입
                path.equals("/v1/auth/login") ||            // 로그인
                path.equals("/v1/auth/send-email") ||       // 이메일 인증코드 전송
                path.equals("/v1/auth/verify-email") ||     // 이메일 인증코드 확인
                path.equals("/v1/auth/check-email") ||      // 이메일 중복 확인
                path.equals(("/swagger-ui.html")) ||        // swagger
                path.startsWith("/swagger-ui") ||           // swagger
                path.startsWith("/v2/api-docs") ||          // swagger
                path.startsWith("/v3/api-docs")             // swagger
        ) {
            // 위 목록에 속하면 다음 필터 실행
            chain.doFilter(request, response);
            return;
        }

        try {
            // 4. 요청 헤더에서 토큰 추출
            String token = resolveToken(httpRequest);

            // 5. 토큰 파싱(=유효성 검사)
            jwtTokenProvider.validateToken(token);

            // 6. 토큰이 정상이면 인증 정보 꺼내서 Spring Security Context에 저장 -> Controller에서 인증된 사용자 정보 사용 가능
            Authentication authentication = jwtTokenProvider.getAuthentication(token);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 7. 다음 필터 실행
            chain.doFilter(request, response);

        } catch (ValidationException e) {
            setErrorResponse(httpResponse, e.getStatus());
        }
    }

}
