<?php

include_once "../../backend/repository/TransactionRepository.php";
include_once "../../backend/dto/TransactionRequestDTO.php";

class TransactionService {
    public function create(TransactionRequestDTO $transactionRequestDTO) {
        return save($transactionRequestDTO);
    }

    public function update($id, TransactionRequestDTO $transactionRequestDTO) {
        return update($id, $transactionRequestDTO);
    }
}
