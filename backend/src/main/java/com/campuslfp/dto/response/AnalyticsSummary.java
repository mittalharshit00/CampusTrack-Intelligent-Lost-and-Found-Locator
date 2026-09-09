package com.campuslfp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummary {
    private long total;
    private long open;
    private long matched;
    private long closed;
}
