<?php

include_once "../../backend/repository/TransactionLocaleRepository.php";
include_once "../../backend/dto/TransactionLocaleRequestDTO.php";
include_once "../../backend/dto/TransactionLocaleDTO.php";

global $repository;
$repository = new TransactionLocaleRepository();

class TransactionLocaleService
{
    public function create(TransactionLocaleRequestDTO $requestDTO): TransactionLocale|string
    {
        global $repository;
        return $repository->save($requestDTO);
    }

    public function update($id, TransactionLocaleRequestDTO $requestDTO): TransactionLocale|string
    {
        global $repository;
        return $repository->update($id, $requestDTO);
    }

    public function findAllByUser($userId): void
    {
        global $repository;
        $transactionLocales = $repository->findAllByUserId($userId);
        $transactionLocaleDTOs = [];

        foreach ($transactionLocales as $transactionLocale)
            $transactionLocaleDTOs[] = $this->buildTransactionLocaleDTO($transactionLocale);

        echo json_encode($transactionLocaleDTOs);
    }

    public function findById($id): void
    {
        global $repository;
        $transactionLocale = $repository->findById($id);
        echo json_encode($this->buildTransactionLocaleDTO($transactionLocale));
    }

    public function delete($id): ?string
    {
        global $repository;
        return $repository->delete($id);
    }

    private function buildTransactionLocaleDTO($transactionLocale): TransactionLocaleDTO
    {
        return new TransactionLocaleDTO(
            $transactionLocale->getId(),
            $transactionLocale->getName(),
            $transactionLocale->getAddress()
        );
    }
}
