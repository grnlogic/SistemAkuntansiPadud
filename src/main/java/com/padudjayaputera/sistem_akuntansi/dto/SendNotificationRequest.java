package com.padudjayaputera.sistem_akuntansi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendNotificationRequest {
    
    @NotBlank(message = "Message tidak boleh kosong")
    private String message;
    
    private String linkUrl; // Optional
}
