package io.nebulas.explorer.model.vo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import io.nebulas.explorer.domain.NebContractToken;
import io.nebulas.explorer.service.thirdpart.nebulas.bean.Event;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * TokenInfo View
 *
 */
@Data
@ToString
public class TokenInfoVo implements Serializable {

    private String tokenName;
    private String description;
    private String contract;
    private BigDecimal total;
    private long tokenDecimals;

    private static final long serialVersionUID = 1L;
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    public TokenInfoVo build(NebContractToken tokenInfo) {
        this.tokenName = tokenInfo.getTokenName();
        this.description = tokenInfo.getDescription();
        this.contract = tokenInfo.getContract();
        this.total = tokenInfo.getTotal();
        this.tokenDecimals = tokenInfo.getTokenDecimals();
        return this;
    }
}

