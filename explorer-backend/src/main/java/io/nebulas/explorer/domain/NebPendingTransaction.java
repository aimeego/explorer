package io.nebulas.explorer.domain;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * explorer.neb_pending_transaction  nebulas pending transaction
 *
 * @author nathan wang
 * @version 1.0
 * @since 2018-01-27
 */
@Builder
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NebPendingTransaction implements Serializable {

    /**
     * hex string of transaction hash
     */
    private String hash;

    /**
     * hex string of the sender account address
     */
    private String from;

    /**
     * hex string of the receiver account address
     */
    private String to;

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


    private Date createdAt;

    /**
     * transaction data
     */
    private String data;

    /**
     * contract address. it is not null, when the type of transaction is deploy.
     */
    private String contractAddress;

    private static final long serialVersionUID = 1L;
}
