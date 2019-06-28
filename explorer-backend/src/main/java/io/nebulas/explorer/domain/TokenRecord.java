package io.nebulas.explorer.domain;


import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TokenRecord implements Serializable {
    private int id;
    private String tokenName;
    private String address;
    private String txHash;
    private long block;
    private String amount;
    private int source;
    private Date timestamp;
    private Date createdAt;
}

