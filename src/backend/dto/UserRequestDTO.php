<?php

class UserRequestDTO {
    private $username;
    private $email;
    private $password;
    private $passwordConfirmation;

    public function __construct($username, $email, $password, $passwordConfirmation) {
        $this->username = $username;
        $this->email = $email;
        $this->password = $password;
        $this->passwordConfirmation = $passwordConfirmation;
    }

    public function getUsername() {
        return $this->username;
    }

    public function setUsername($username) {
        $this->username = $username;
    }

    public function getEmail() {
        return $this->email;
    }

    public function setEmail($email) {
        $this->email = $email;
    }

    public function getPassword() {
        return $this->password;
    }

    public function setPassword($password) {
        $this->password = $password;
    }

    public function getPasswordConfirmation() {
        return $this->passwordConfirmation;
    }

    public function setPasswordConfirmation($passwordConfirmation) {
        $this->passwordConfirmation = $passwordConfirmation;
    }
}
