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
 * 项目实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("project")
public class Project {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    // application.yml 配了 map-underscore-to-camel-case: true，所以这里不需要下划线前缀
    private String gitUrl;

    private String branchName;

    private Long creatorId;

    private Integer status;

    private LocalDateTime createTime;
}
