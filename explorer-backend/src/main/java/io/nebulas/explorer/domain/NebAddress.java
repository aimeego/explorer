package io.nebulas.explorer.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * explorer.neb_address  nebulas address
 * <p>
 * Description.
 *
 * @author nathan wang
 * @version 1.0
 * @since 2018-01-25
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NebAddress implements Serializable {
    /**
     * primary key
     */
    private Long id;

    /**
     * hex string of address hash
     */
    private String hash;

    /**
     * current transaction count
     */
    private String nonce;

    /**
     * address type, 0: Normal; 1: Contract
     */
    private Integer type;

    /**
     * address alias
     */
    private String alias;

    /**
     * address current balance
     */
    private BigDecimal currentBalance;

    private Date createdAt;

    private Date updatedAt;

    private static final long serialVersionUID = 1L;

    public NebAddress(String hash) {
        this.hash = hash;
    }

    public String getBalance() {
        if (null == currentBalance) {
            return "0";
        }
        return currentBalance.stripTrailingZeros().toPlainString();
    }
}
