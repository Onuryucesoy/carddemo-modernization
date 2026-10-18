package com.carddemo.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key of {@link TransactionCategory}.
 */
public class TransactionCategoryId implements Serializable {

    private String tranTypeCd;
    private Integer tranCatCd;

    public TransactionCategoryId() {
    }

    public TransactionCategoryId(String tranTypeCd, Integer tranCatCd) {
        this.tranTypeCd = tranTypeCd;
        this.tranCatCd = tranCatCd;
    }

    public String getTranTypeCd() {
        return tranTypeCd;
    }

    public Integer getTranCatCd() {
        return tranCatCd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionCategoryId other)) {
            return false;
        }
        return Objects.equals(tranTypeCd, other.tranTypeCd) && Objects.equals(tranCatCd, other.tranCatCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tranTypeCd, tranCatCd);
    }
}
