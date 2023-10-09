<?php

include_once "../../backend/repository/TransactionLocaleRepository.php";
include_once "../../backend/dto/TransactionLocaleRequestDTO.php";

class TransactionLocaleService {
    public function create(TransactionLocaleRequestDTO $transactionLocaleRequestDTO) {
        return save($transactionLocaleRequestDTO);
    }

    public function update($id, TransactionLocaleRequestDTO $transactionLocaleRequestDTO) {
        return update($id, $transactionLocaleRequestDTO);
    }
}
