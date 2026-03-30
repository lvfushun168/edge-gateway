package com.ghq.edgegateway.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 鉴权响应载荷。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponsePayload {

    private Integer code;

    private String msg;
}
