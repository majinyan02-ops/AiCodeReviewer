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
 * 审查任务实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("review_task")
public class ReviewTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String taskName;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime createTime;
}
