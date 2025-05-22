package notfound.ballog.domain.quarter.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import notfound.ballog.domain.quarter.dto.ReportData;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddQuarterAndGameReportRequest {

    @NotNull
    private Integer matchId;

    @NotNull
    private List<ReportData> reportDataList;

}
