package com.carddemo.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key of {@link TransactionCategoryBalance}.
 */
public class TransactionCategoryBalanceId implements Serializable {

    private Long acctId;
    private String typeCd;
    private Integer catCd;

    public TransactionCategoryBalanceId() {
    }

    public TransactionCategoryBalanceId(Long acctId, String typeCd, Integer catCd) {
        this.acctId = acctId;
        this.typeCd = typeCd;
        this.catCd = catCd;
    }

    public Long getAcctId() {
        return acctId;
    }

    public String getTypeCd() {
        return typeCd;
    }

    public Integer getCatCd() {
        return catCd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionCategoryBalanceId other)) {
            return false;
        }
        return Objects.equals(acctId, other.acctId) && Objects.equals(typeCd, other.typeCd) && Objects.equals(catCd, other.catCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acctId, typeCd, catCd);
    }
}
