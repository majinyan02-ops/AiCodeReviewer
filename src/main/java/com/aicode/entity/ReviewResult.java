package com.aicode.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审查结果实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("review_result")
public class ReviewResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private String filePath;

    private String ruleCode;

    private String riskLevel;

    private String problemDesc;

    private String aiAnalysis;

    private String repairSuggestion;

    private LocalDateTime createTime;
}
