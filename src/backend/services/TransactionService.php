<?php

include_once "../../backend/repository/TransactionRepository.php";
include_once "../../backend/dto/TransactionRequestDTO.php";

class TransactionService {
    public function create(TransactionRequestDTO $requestDTO) {
        return save($requestDTO);
    }

    public function update($id, TransactionRequestDTO $requestDTO) {
        return update($id, $requestDTO);
    }
}
