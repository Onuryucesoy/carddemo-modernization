package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Copybook {@code CVEXPORT}, record {@code EXPORT-RECORD header (EXPFILE, RECLN 500)}.
 */
@Entity
@Table(name = "export_record")
public class ExportRecord {

    /** {@code EXPORT-SEQUENCE-NUM} {@code 9(9) COMP} */
    @Id
    @Column(name = "export_sequence_num", nullable = false)
    private Long sequenceNum;

    /** {@code EXPORT-REC-TYPE} {@code X(1)} */
    @Column(name = "export_rec_type", length = 1)
    private Character recType;

    /** {@code EXPORT-TIMESTAMP} {@code X(26)} */
    @Column(name = "export_timestamp")
    private LocalDateTime exportTimestamp;

    /** {@code EXPORT-BRANCH-ID} {@code X(4)} */
    @Column(name = "export_branch_id", length = 4)
    private String branchId;

    /** {@code EXPORT-REGION-CODE} {@code X(5)} */
    @Column(name = "export_region_code", length = 5)
    private String regionCode;

    /** {@code EXP-CUST-ID / EXP-ACCT-ID / EXP-XREF-CARD-NUM / EXP-TRAN-ID / EXP-CARD-NUM} {@code key of EXPORT-RECORD-DATA} */
    @Column(name = "export_record_key", length = 16)
    private String recordKey;

    public Long getSequenceNum() {
        return sequenceNum;
    }

    public void setSequenceNum(Long sequenceNum) {
        this.sequenceNum = sequenceNum;
    }

    public Character getRecType() {
        return recType;
    }

    public void setRecType(Character recType) {
        this.recType = recType;
    }

    public LocalDateTime getExportTimestamp() {
        return exportTimestamp;
    }

    public void setExportTimestamp(LocalDateTime exportTimestamp) {
        this.exportTimestamp = exportTimestamp;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getRecordKey() {
        return recordKey;
    }

    public void setRecordKey(String recordKey) {
        this.recordKey = recordKey;
    }
}
