package notfound.ballog.domain.quarter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportData {

    @NotNull
    private Integer quarterNumber;

    @NotBlank(message = "경기 진영을 선택해주세요")
    private String gameSide;

    @NotNull
    private Map<String, Object> gameReportData;

}
