<?php

include_once "../../backend/repository/UserRepository.php";
include_once "../../backend/dto/UserRequestDTO.php";

global $repository;
$repository = new UserRepository();

class UserService
{
    public function create(UserRequestDTO $requestDTO)
    {
        global $repository;
        if (findByEmail($requestDTO->getEmail())) {
            if ($this->verifyPasswordsEquality($requestDTO->getPassword(), $requestDTO->getPasswordConfirmation()))
                return $repository->save($requestDTO);
            else return "As senhas devem ser iguais";
        } else return "Email Já cadastrado";
    }

    private function verifyPasswordsEquality($password, $password_confirmation)
    {
        return $password == $password_confirmation;
    }

    public function update($id, UserRequestDTO $requestDTO)
    {
        global $repository;
        if (findByEmail($requestDTO->getEmail())) {
            if ($this->verifyPasswordsEquality($requestDTO->getPassword(), $requestDTO->getPasswordConfirmation()))
                return $repository->update($id, $requestDTO);
            else return "As senhas devem ser iguais";
        } else return "Email Já cadastrado";
    }

    public function login($userEmail, $userPassword)
    {
        global $repository;
        if ($user = $repository->findByEmailAndPassword($userEmail, $userPassword))
            return $user;
        else return "Email e/ou senha incorreto(s)";
    }
}
