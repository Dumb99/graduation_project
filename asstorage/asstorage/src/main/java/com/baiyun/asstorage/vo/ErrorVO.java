package com.baiyun.asstorage.vo;

import com.baiyun.asstorage.enu.ErrorResultEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
@Data
@NoArgsConstructor
public class ErrorVO{
    private Set<String> errAs;
    private String errorType;
    private String errorMsg;
    public ErrorVO(String errorMsg){
        this.errorMsg = errorMsg;
    }

    public ErrorVO(Set<String> errAs, ErrorResultEnum errorResultEnum){
        this.errorType = errorResultEnum.getMsg();
        String prefix = "AS%s ";
        StringBuilder stringBuilder = new StringBuilder();
        this.errAs = errAs;
        for (String index : errAs) {
            stringBuilder.append(String.format(prefix, index));
        }
        switch (errorResultEnum){
            case ADJACENT_REPEAT: stringBuilder.append("重复出现");break;
            case AS_LOOP: stringBuilder.append("多次出现");break;
            case NATION_LEAK: stringBuilder.append("分别为国内外AS");break;
            case FAKE_AS: stringBuilder.append("不存在邻居关系");break;
        }
        stringBuilder.append(",存在").append(errorResultEnum.getMsg()).append("！");
        this.errorMsg = stringBuilder.toString();
    }

    public Set<String> getErrAs(){
        if (Objects.isNull(errAs)){
            errAs = new HashSet<>();
        }
        return errAs;
    }

}
