package com.padudjayaputera.sistem_akuntansi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.padudjayaputera.sistem_akuntansi.model.Account;
import com.padudjayaputera.sistem_akuntansi.model.ValueType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponse {
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("accountCode")
    private String accountCode;
    
    @JsonProperty("accountName")
    private String accountName;
    
    @JsonProperty("valueType")
    private ValueType valueType;
    
    @JsonProperty("division")
    private DivisionDto division;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DivisionDto {
        @JsonProperty("id")
        private Integer id;
        
        @JsonProperty("name")
        private String name;
    }
    
    // Constructor yang menerima Account entity
    public AccountResponse(Account account) {
        this.id = account.getId();
        this.accountCode = account.getAccountCode();
        this.accountName = account.getAccountName();
        this.valueType = account.getValueType();
        this.division = DivisionDto.builder()
            .id(account.getDivision().getId())
            .name(account.getDivision().getName())
            .build();
    }
    
    // Static method untuk convert dari Account entity
    public static AccountResponse fromAccount(Account account) {
        DivisionDto divisionDto = DivisionDto.builder()
            .id(account.getDivision().getId())
            .name(account.getDivision().getName())
            .build();
            
        return AccountResponse.builder()
            .id(account.getId())
            .accountCode(account.getAccountCode())
            .accountName(account.getAccountName())
            .valueType(account.getValueType())
            .division(divisionDto)
            .build();
    }
}