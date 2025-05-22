package notfound.ballog.domain.quarter.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GameReportDto {

    private Integer quarterNumber;

    private Map<String, Object> reportData;

    private String matchSide;

}
