<?php

include_once "../../backend/repository/TransactionLocaleRepository.php";
include_once "../../backend/dto/TransactionLocaleRequestDTO.php";
include_once "../../backend/dto/TransactionLocaleDTO.php";

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

    public function findAllByUser($userId) {
        global $repository;
        $transactionLocales = $repository->findAllByUserId($userId);
        $transactionLocaleDTOs = [];

        foreach ($transactionLocales as $transactionLocale) {
            $transactionLocaleDTOs[] = new TransactionLocaleDTO(
                $transactionLocale->getId(),
                $transactionLocale->getName(),
                $transactionLocale->getAddress()
            );
        }

        echo json_encode($transactionLocaleDTOs);
    }
}
