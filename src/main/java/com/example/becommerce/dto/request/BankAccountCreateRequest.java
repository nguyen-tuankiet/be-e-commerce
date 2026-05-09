package com.example.becommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body for POST /api/wallet/bank-accounts
 */
@Getter
@Setter
public class BankAccountCreateRequest {

    @NotBlank(message = "Tên ngân hàng không được để trống")
    private String bankName;

    @NotBlank(message = "Số tài khoản không được để trống")
    @Pattern(regexp = "^[0-9]{6,20}$", message = "Số tài khoản ngân hàng không hợp lệ")
    private String accountNumber;

    @NotBlank(message = "Tên chủ tài khoản không được để trống")
    @Pattern(regexp = "^[A-Z0-9À-Ỹ' .-]+$", message = "Tên chủ tài khoản phải viết hoa")
    private String accountOwner;
}
