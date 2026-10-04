package com.example.bookspot;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserProfile {

    @PrimaryKey
    @NonNull
    private String accountNumber;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String joinDate;
    private String pinCode;

    public UserProfile(@NonNull String accountNumber, String fullName, String email,
                       String phoneNumber, String joinDate, String pinCode) {
        this.accountNumber = accountNumber;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.joinDate = joinDate;
        this.pinCode = pinCode;
    }

    @NonNull
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(@NonNull String accountNumber) { this.accountNumber = accountNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getJoinDate() { return joinDate; }
    public void setJoinDate(String joinDate) { this.joinDate = joinDate; }

    public String getPinCode() { return pinCode; }
    public void setPinCode(String pinCode) { this.pinCode = pinCode; }
}