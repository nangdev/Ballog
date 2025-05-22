package notfound.ballog.domain.quarter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import notfound.ballog.common.response.BaseResponse;
import notfound.ballog.domain.quarter.request.AddQuarterAndGameReportRequest;
import notfound.ballog.domain.quarter.response.AddQuarterAndGameReportResponse;
import notfound.ballog.domain.quarter.service.QuarterService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/quarter")
@Tag(name = "Quarter", description = "쿼터, 게임기록 관련 API")
public class QuarterController {

    private final QuarterService quarterService;

    @PostMapping
    @Operation(summary = "쿼터 등록")
    public BaseResponse<AddQuarterAndGameReportResponse> addQuarterAndGameReport(@AuthenticationPrincipal UUID userId, @RequestBody AddQuarterAndGameReportRequest addQuarterAndGameReportRequest) {
        return BaseResponse.ok(quarterService.addQuarterAndGameReport(userId, addQuarterAndGameReportRequest));
    }

}
