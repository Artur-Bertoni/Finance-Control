<?php

include_once "../../backend/repository/TransactionLocaleRepository.php";
include_once "../../backend/dto/TransactionLocaleRequestDTO.php";

global $repository;
$repository = new TransactionLocaleRepository();
class TransactionLocaleService {
    public function create(TransactionLocaleRequestDTO $requestDTO) {
        global $repository;
        return $repository->save($requestDTO);
    }

    public function update($id, TransactionLocaleRequestDTO $requestDTO) {
        global $repository;
        return $repository->update($id, $requestDTO);
    }
}
