package notfound.ballog.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notfound.ballog.common.response.BaseResponse;
import notfound.ballog.domain.auth.request.LoginRequest;
import notfound.ballog.domain.auth.request.SendEmailRequest;
import notfound.ballog.domain.auth.request.VerifyEmailRequest;
import notfound.ballog.domain.auth.response.CheckEmailResponse;
import notfound.ballog.domain.auth.response.LoginResponse;
import notfound.ballog.domain.auth.response.TokenRefreshResponse;
import notfound.ballog.domain.auth.service.AuthService;
import notfound.ballog.domain.auth.request.SignUpRequest;
import notfound.ballog.domain.auth.service.EmailService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@Tag(
        name = "Auth"
)
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    @Operation(summary = "회원가입", description = "인증 필요 없음")
    @PostMapping("/signup")
    public BaseResponse<Void> addUser(@Valid @RequestBody SignUpRequest request){
        authService.signUp(request);

        return BaseResponse.ok();
    }


    @Operation(summary = "로그인", description = "인증 필요 없음")
    @PostMapping("/login")
    public BaseResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        LoginResponse response = authService.login(request);

        return BaseResponse.ok(response);
    }


    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public BaseResponse<Void> logout(@AuthenticationPrincipal UUID userId){
        authService.logOut(userId);

        return BaseResponse.ok();
    }


    @Operation(summary = "토큰 재발급")
    @PostMapping("/refresh-token")
    public BaseResponse<TokenRefreshResponse> refreshToken(
            @AuthenticationPrincipal UUID userId,
            @RequestHeader("Authorization") String header
    ) {
        String refreshToken = header.replace("Bearer ", "");

        TokenRefreshResponse response = authService.refreshToken(userId, refreshToken);

        return BaseResponse.ok(response);
    }


    @Operation(summary = "이메일 중복 확인", description = "인증 필요 없음")
    @GetMapping("/check-email")
    public BaseResponse<CheckEmailResponse> checkEmail(@RequestParam("email")
                                                       @Email(message = "올바른 이메일을 입력하세요.")
                                                       @NotBlank(message = "이메일을 입력하세요.")
                                                       String email
    ){
        CheckEmailResponse response = authService.checkEmail(email);

        return BaseResponse.ok(response);
    }


    @Operation(summary = "회원탈퇴")
    @PostMapping("/signout")
    public BaseResponse<Void> signOut(@AuthenticationPrincipal UUID userId) {
        authService.signOut(userId);

        return BaseResponse.ok();
    }


    @Operation(summary = "이메일 인증 코드 요청", description = "인증 필요 없음")
    @PostMapping("/send-email")
    public BaseResponse<Void> sendEmailCode(@Valid @RequestBody SendEmailRequest request) {
        emailService.sendEmailCode(request);

        return BaseResponse.ok();
    }


    @Operation(summary = "이메일 인증 코드 확인", description = "인증 필요 없음")
    @PostMapping("/verify-email")
    public BaseResponse<Void> verifyEmailCode(@Valid @RequestBody VerifyEmailRequest request) {
        emailService.verifyEmailCode(request);

        return BaseResponse.ok();
    }


}
