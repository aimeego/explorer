package io.nebulas.explorer.domain;

import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * explorer.neb_transaction  nebulas transaction
 *
 * @author nathan wang
 * @version 1.0
 * @since 2018-01-25
 */
@Builder
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NebTransaction implements Serializable {
    /**
     * hex string of transaction hash
     */
    private String hash;

    /**
     * hex string of block hash
     */
    private String blockHash;

    /**
     * block height
     */
    private Long blockHeight;

    /**
     * tx sequence in same block
     */
    private Integer txSeq;
    /**
     * hex string of the sender account address
     */
    private String from;

    /**
     * hex string of the receiver account address
     */
    private String to;

    /**
     * transaction status, 0: failed; 1: success; 2: pending;
     */
    private Integer status;

    /**
     * value of transaction
     */
    private String value;

    /**
     * transaction nonce
     */
    private Long nonce;

    /**
     * transaction timestamp
     */
    private Date timestamp;

    /**
     * transaction type
     */
    private String type;

    /**
     * Gas price
     */
    private String gasPrice;

    /**
     * Gas limit
     */
    private String gasLimit;

    /**
     * Gas used
     */
    private String gasUsed;

    private Date createdAt;

    /**
     * transaction data
     */
    private String data;

    /**
     * contract address. it is not null, when the type of transaction is deploy.
     */
    private String contractAddress;

    private String executeError;

    private static final long serialVersionUID = 1L;

    public String getTxFee() {
        if (StringUtils.isEmpty(gasUsed) || StringUtils.isEmpty(gasPrice)) {
            return "";
        }
        return new BigDecimal(gasUsed).multiply(new BigDecimal(gasPrice)).toPlainString();
    }

    public BigDecimal getGasUsedBd() {
        return StringUtils.isEmpty(this.gasUsed) ? BigDecimal.ZERO : new BigDecimal(this.gasUsed);
    }

    public BigDecimal getGasPriceBd() {
        return StringUtils.isEmpty(this.gasPrice) ? BigDecimal.ZERO : new BigDecimal(this.gasPrice);
    }

    public BigDecimal getGasLimitBd() {
        return StringUtils.isEmpty(this.gasLimit) ? BigDecimal.ZERO : new BigDecimal(this.gasLimit);
    }
}
