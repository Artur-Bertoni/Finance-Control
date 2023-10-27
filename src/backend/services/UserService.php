<?php

include_once "../../backend/repository/UserRepository.php";
include_once "../../backend/dto/UserRequestDTO.php";
include_once "../../backend/dto/UserDTO.php";

global $repository;
$repository = new UserRepository();

class UserService
{
    public function create(UserRequestDTO $requestDTO): User|string
    {
        global $repository;

        if ($repository->findByEmail($requestDTO->getEmail())) {
            if ($this->verifyPasswordsEquality($requestDTO->getPassword(), $requestDTO->getPasswordConfirmation()))
                return $repository->save($requestDTO);
            else return "As senhas devem ser iguais";
        } else return "Email já cadastrado";
    }

    private function verifyPasswordsEquality($password, $password_confirmation): bool
    {
        return $password == $password_confirmation;
    }

    public function update($id, UserRequestDTO $requestDTO): User|string
    {
        global $repository;
        if (!$repository->findByEmailForUpdate($id, $requestDTO->getEmail())) {
            if ($this->verifyPasswordsEquality($requestDTO->getPassword(), $requestDTO->getPasswordConfirmation()))
                return $repository->update($id, $requestDTO);
            else return "As senhas devem ser iguais";
        } else return "Email já cadastrado";
    }

    public function login($userEmail, $userPassword): User|string
    {
        global $repository;

        if ($user = $repository->findByEmailAndPassword($userEmail, $userPassword))
            return $user;
        else return "Email e/ou senha incorreto(s)";
    }

    public function findById($id): void
    {
        global $repository;
        $user = $repository->findById($id);
        echo json_encode($this->buildUserDTO($user));
    }

    private function buildUserDTO($user): UserDTO
    {
        return new UserDTO(
            $user->getId(),
            $user->getUsername(),
            $user->getEmail(),
            $user->getPassword(),
        );
    }

    public function delete($id): ?string
    {
        global $repository;
        return $repository->delete($id);
    }
}
