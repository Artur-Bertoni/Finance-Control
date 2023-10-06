<?php

include_once "../../backend/repository/UserRepository.php";
include_once "../../backend/dto/UserRequestDTO.php";

class UserService {
    public function create(UserRequestDTO $userRequestDTO) {
        if (findByEmail($userRequestDTO->getEmail())) {
            if ($this->verifyPasswordsEquality($userRequestDTO->getPassword(), $userRequestDTO->getPasswordConfirmation()))
                return save($userRequestDTO);
            else return "As senhas devem ser iguais";
        } else return "Email Já cadastrado";
    }

    public function update($id, UserRequestDTO $userRequestDTO) {
        if (findByEmail($userRequestDTO->getEmail())) {
            if ($this->verifyPasswordsEquality($userRequestDTO->getPassword(), $userRequestDTO->getPasswordConfirmation()))
                return update($id, $userRequestDTO);
            else return "As senhas devem ser iguais";
        } else return "Email Já cadastrado";
    }

    private function verifyPasswordsEquality($password, $password_confirmation) {
        return $password == $password_confirmation;
    }

    public function login($userEmail, $userPassword) {
        if ($user = findByEmailAndPassword($userEmail, $userPassword))
            return $user;
        else return "Email e/ou senha incorreto(s)";
    }
}
