package com.padudjayaputera.sistem_akuntansi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.padudjayaputera.sistem_akuntansi.model.ValueType;

import lombok.Data;

@Data
public class AccountCreateRequest {
    
    @JsonProperty("accountCode")
    private String accountCode;
    
    @JsonProperty("accountName")
    private String accountName;
    
    @JsonProperty("valueType")
    private ValueType valueType;
    
    @JsonProperty("division")
    private DivisionDto division;
    
    @Data
    public static class DivisionDto {
        @JsonProperty("id")
        private Integer id;
        
        @JsonProperty("name")
        private String name;
    }
    
    // Constructor untuk debugging
    public AccountCreateRequest() {
        System.out.println("AccountCreateRequest created");
    }
    
    // Override setter untuk debugging
    public void setAccountCode(String accountCode) {
        System.out.println("Setting accountCode: " + accountCode);
        this.accountCode = accountCode;
    }
    
    public void setAccountName(String accountName) {
        System.out.println("Setting accountName: " + accountName);
        this.accountName = accountName;
    }
    
    public void setValueType(ValueType valueType) {
        System.out.println("Setting valueType: " + valueType);
        this.valueType = valueType;
    }
    
    public void setDivision(DivisionDto division) {
        System.out.println("Setting division: " + division);
        this.division = division;
    }
}