package com.example.bookspot;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserProfile user);

    @Update
    void updateUser(UserProfile user);

    @Delete
    void deleteUser(UserProfile user);

    @Query("SELECT * FROM users WHERE email = :email AND pinCode = :pin LIMIT 1")
    UserProfile login(String email, String pin);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserProfile getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE accountNumber = :accNum LIMIT 1")
    UserProfile getUserByAccount(String accNum);

    @Query("SELECT * FROM users")
    List<UserProfile> getAllUsers();
}