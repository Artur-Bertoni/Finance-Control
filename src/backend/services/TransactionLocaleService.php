<?php

include_once "../../backend/repository/TransactionLocaleRepository.php";
include_once "../../backend/dto/TransactionLocaleRequestDTO.php";

class TransactionLocaleService {
    public function create(TransactionLocaleRequestDTO $requestDTO) {
        return save($requestDTO);
    }

    public function update($id, TransactionLocaleRequestDTO $requestDTO) {
        return update($id, $requestDTO);
    }
}
