package com.example.group_33_project;

public class AuthenticationException extends Exception { // Custom exception class for invalid credentials (wrong email/password, no account made)
  public AuthenticationException() {
      super("Authentication failed");
  }
    public AuthenticationException(String message) {
        super(message);
    }
}
