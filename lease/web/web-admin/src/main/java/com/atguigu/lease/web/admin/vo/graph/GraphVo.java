package com.atguigu.lease.web.admin.vo.graph;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@Schema(description = "图片信息")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GraphVo implements Serializable {
    private static final long serialVersionUID = 1L;


    @Schema(description = "图片名称")
    private String name;

    @Schema(description = "图片地址")
    private String url;

}
