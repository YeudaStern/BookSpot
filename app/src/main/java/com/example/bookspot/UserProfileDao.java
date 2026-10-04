package com.example.bookspot;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserProfile user);

    @Update
    void updateUser(UserProfile user);

    @Delete
    void deleteUser(UserProfile user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserProfile getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE email = :email AND pinCode = :pinCode LIMIT 1")
    UserProfile login(String email, String pinCode);

    @Query("SELECT * FROM users")
    LiveData<List<UserProfile>> getAllUsers();

    @Query("DELETE FROM users")
    void deleteAllUsers();
}