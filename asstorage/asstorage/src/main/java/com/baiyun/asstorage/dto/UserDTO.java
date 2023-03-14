package com.baiyun.asstorage.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@TableName("user")
public class UserDTO {
    private Long id;
    private String email;
    private String nickName;
}
