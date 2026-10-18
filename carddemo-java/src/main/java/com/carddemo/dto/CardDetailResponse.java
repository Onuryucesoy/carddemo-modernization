package com.carddemo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Card lookup result. The top-level fields carry the COMMAREA ({@code COCOM01Y}) values that
 * COCRDSLC hands to the next program; the nested objects carry the resolved records.
 *
 * @param acctId      {@code CDEMO-ACCT-ID}
 * @param acctStatus  {@code CDEMO-ACCT-STATUS}
 * @param cardNum     {@code CDEMO-CARD-NUM}
 * @param custId      {@code CDEMO-CUST-ID}
 * @param custFname   {@code CDEMO-CUST-FNAME}
 * @param custMname   {@code CDEMO-CUST-MNAME}
 * @param custLname   {@code CDEMO-CUST-LNAME}
 * @param infoMessage {@code WS-INFO-MSG} ({@code FOUND-CARDS-FOR-ACCOUNT})
 */
public record CardDetailResponse(
        Long acctId,
        Character acctStatus,
        String cardNum,
        Long custId,
        String custFname,
        String custMname,
        String custLname,
        CardInfo card,
        AccountInfo account,
        CustomerInfo customer,
        String infoMessage) {

    /**
     * The COCRDSL screen output fields. CVV is not returned (the screen never shows it).
     *
     * @param embossedName   {@code CRDNAMEO}
     * @param expiryMonth    {@code EXPMONO}
     * @param expiryYear     {@code EXPYEARO}
     * @param activeStatus   {@code CRDSTCDO}
     */
    public record CardInfo(String embossedName, LocalDate expirationDate, String expiryMonth, String expiryYear,
                           Character activeStatus) {
    }

    public record AccountInfo(Character activeStatus, BigDecimal currBal, BigDecimal creditLimit,
                              BigDecimal cashCreditLimit, LocalDate openDate, LocalDate expirationDate,
                              LocalDate reissueDate, BigDecimal currCycCredit, BigDecimal currCycDebit,
                              String addrZip, String groupId) {
    }

    /** SSN, government ID and EFT account are deliberately not returned. */
    public record CustomerInfo(String firstName, String middleName, String lastName, String addrLine1,
                               String addrLine2, String addrLine3, String addrStateCd, String addrCountryCd,
                               String addrZip, String phoneNum1, String phoneNum2, LocalDate dob,
                               Character priCardHolderInd, Integer ficoScore) {
    }
}
