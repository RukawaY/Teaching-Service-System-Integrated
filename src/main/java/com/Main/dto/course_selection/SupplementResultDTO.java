package com.Main.dto.course_selection;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 补选结果数据传输对象
 */
public class SupplementResultDTO {
    private Integer supplementId;
    private Boolean result;

    @JsonProperty("supplement_id")
    public Integer getSupplementId() {
        return supplementId;
    }

    public void setSupplementId(Integer supplementId) {
        this.supplementId = supplementId;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }
}