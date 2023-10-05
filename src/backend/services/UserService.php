<?php

include_once "../../backend/repository/UserRepository.php";
include_once "../../backend/dto/UserRequestDTO.php";

class UserService {
    public function create(UserRequestDTO $userRequestDTO) {
        if (findByEmail($userRequestDTO->getEmail())->num_rows == 0) {
            if ($this->verifyPasswordsEquality($userRequestDTO->getPassword(), $userRequestDTO->getPasswordConfirmation()))
                return save($userRequestDTO);
            else {
                echo "<script>alert('As senhas devem ser iguais');location.href=\"../User.html\";</script>";
                return null;
            }
        } else {
            echo "<script>alert('Email Já cadastrado');location.href=\"../User.html\";</script>";
            return null;
        }
    }

    private function verifyPasswordsEquality($password, $password_confirmation) {
        return $password == $password_confirmation;
    }

    public function login($userEmail, $userPassword) {
        $result = findByEmailAndPassword($userEmail, $userPassword);
        if ($result) {
            $user = $result->fetch_assoc();
            return new User($user['id'], $user['username'], $user['email'], $user['password']);
        } else {
            echo "<script>alert('Email e/ou senha incorreto(s)');location.href=\"../Login.html\";</script>";
            return null;
        }
    }
}
