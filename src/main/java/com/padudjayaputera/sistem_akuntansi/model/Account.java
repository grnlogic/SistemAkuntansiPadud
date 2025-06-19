package com.padudjayaputera.sistem_akuntansi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "account_code", nullable = false, unique = true)
    @JsonProperty("accountCode")
    private String accountCode;

    @Column(name = "account_name", nullable = false)
    @JsonProperty("accountName")
    private String accountName;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false)
    @JsonProperty("valueType")
    private ValueType valueType;

    // PENTING: Ubah ke EAGER fetch untuk menghindari lazy loading issues
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "division_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Division division;

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", accountCode='" + accountCode + '\'' +
                ", accountName='" + accountName + '\'' +
                ", valueType=" + valueType +
                ", division=" + (division != null ? division.getId() : "null") +
                '}';
    }
}