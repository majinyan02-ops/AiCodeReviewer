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
 * 审查报告实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("review_report")
public class ReviewReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private String reportContent;

    private String reportUrl;

    private LocalDateTime createTime;
}
