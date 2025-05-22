package notfound.ballog.domain.auth.service;

import lombok.RequiredArgsConstructor;
import notfound.ballog.common.response.BaseResponseStatus;
import notfound.ballog.domain.auth.entity.Auth;
import notfound.ballog.domain.auth.repository.AuthRepository;
import notfound.ballog.exception.ValidationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthRepository authRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String email){
        Auth auth = authRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException(BaseResponseStatus.USER_NOT_FOUND));

        return new CustomUserDetails(auth);
    }

    // 이메일로 유저 조회
    public CustomUserDetails loadUserByEmail(String email){
        Auth auth = authRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ValidationException(BaseResponseStatus.USER_NOT_FOUND));

        return new CustomUserDetails(auth);
    }

    // AuthId로 유저 조회
    public CustomUserDetails loadUserByAuthId(Integer authId) {
        Auth auth = authRepository.findByAuthIdAndIsActiveTrue(authId)
                .orElseThrow(() -> new ValidationException(BaseResponseStatus.USER_NOT_FOUND));

        return new CustomUserDetails(auth);
    }
}
