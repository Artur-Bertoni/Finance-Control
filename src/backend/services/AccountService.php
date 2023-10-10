<?php

include_once "../../backend/repository/AccountRepository.php";
include_once "../../backend/dto/AccountRequestDTO.php";

global $repository;
$repository = new AccountRepository();
class AccountService {
    public function create(AccountRequestDTO $requestDTO) {
        global $repository;
        return $repository->save($requestDTO);
    }

    public function update($id, AccountRequestDTO $requestDTO) {
        global $repository;
        return $repository->update($id, $requestDTO);
    }
}
