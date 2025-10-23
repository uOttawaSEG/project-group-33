package com.example.group_33_project;
import java.util.List;
// in order to retrieve a list of pending accounts (for admin)

public interface PendingCallback {
    void onSuccess(List<Account> accounts);
    void onFailure(String msg);
}
