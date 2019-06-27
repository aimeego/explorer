package io.nebulas.explorer.domain;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * explorer.neb_block  nebulas block
 *
 * @author nathan wang
 * @version 1.0
 * @since 2018-1-25
 */
@Builder
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NebBlock implements Serializable {
    /**
     * hex string of block hash
     */
    private String hash;

    /**
     * block height
     */
    private Long height;

    /**
     * block timestamp
     */
    private Date timestamp;

    /**
     * hex string of block parent hash
     */
    private String parentHash;

    /**
     * hex string of miner address
     */
    private String miner;

    /**
     * hex string of coinbase address
     */
    private String coinbase;

    /**
     * block is irreversible
     */
    private Boolean finality;

    private Date createdAt;

    private static final long serialVersionUID = 1L;

}
