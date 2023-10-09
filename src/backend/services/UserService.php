<?php

include_once "../../backend/repository/UserRepository.php";
include_once "../../backend/dto/UserRequestDTO.php";

class UserService {
    public function create(UserRequestDTO $requestDTO) {
        if (findByEmail($requestDTO->getEmail())) {
            if ($this->verifyPasswordsEquality($requestDTO->getPassword(), $requestDTO->getPasswordConfirmation()))
                return save($requestDTO);
            else return "As senhas devem ser iguais";
        } else return "Email Já cadastrado";
    }

    public function update($id, UserRequestDTO $requestDTO) {
        if (findByEmail($requestDTO->getEmail())) {
            if ($this->verifyPasswordsEquality($requestDTO->getPassword(), $requestDTO->getPasswordConfirmation()))
                return update($id, $requestDTO);
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
