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
 * 规则配置实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("rule_config")
public class RuleConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ruleCode;

    private String ruleName;

    private String description;

    private Integer enabled;

    private LocalDateTime createTime;
}
