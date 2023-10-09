<?php

include_once "../../backend/repository/AccountRepository.php";
include_once "../../backend/dto/AccountRequestDTO.php";

class AccountService {
    public function create(AccountRequestDTO $requestDTO) {
        return save($requestDTO);
    }

    public function update($id, AccountRequestDTO $requestDTO) {
        return update($id, $requestDTO);
    }
}
