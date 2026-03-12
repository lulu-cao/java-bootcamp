package com.wcc.platform;

import com.wcc.platform.validation.EmailValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {

    @Test
    void shouldValidateCorrectEmail() {
        //Arrange
        String email = "test@gmail.com";

        //Act
        boolean result = EmailValidator.isValid(email);

        //Assert
        assertTrue(result);

    }

    @Test
    void shouldRejectInvalidEmail() {
        //Arrange
        String email = "email.com";

        //Act
        boolean result = EmailValidator.isValid(email);

        //Assert
        assertFalse(result, "Email is invalid");
    }
}