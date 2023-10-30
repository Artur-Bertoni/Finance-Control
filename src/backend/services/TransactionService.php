<?php

include_once "../../backend/repository/TransactionRepository.php";
include_once "../../backend/repository/AccountRepository.php";
include_once "../../backend/repository/TransactionLocaleRepository.php";
include_once "../../backend/repository/CategoryRepository.php";
include_once "../../backend/dto/TransactionRequestDTO.php";
include_once "../../backend/dto/TransactionDTO.php";

global $repository, $accountRepository, $categoryRepository, $transactionLocaleRepository;
$repository = new TransactionRepository();
$accountRepository = new AccountRepository();
$transactionLocaleRepository = new TransactionLocaleRepository();
$categoryRepository = new CategoryRepository();

class TransactionService
{
    public function create(TransactionRequestDTO $requestDTO): Transaction|string
    {
        global $repository;
        return $repository->save($requestDTO);
    }

    public function update($id, TransactionRequestDTO $requestDTO): Transaction|string
    {
        global $repository;
        return $repository->update($id, $requestDTO);
    }

    public function findAllByUser($userId): void
    {
        global $repository;
        $transactions = $repository->findAllByUserId($userId);
        $transactionDTOs = [];

        foreach ($transactions as $transaction)
            $transactionDTOs[] = $this->buildTransactionDTO($transaction);

        echo json_encode($transactionDTOs);
    }

    public function findById($id): void
    {
        global $repository;
        $transaction = $repository->findById($id);
        echo json_encode($this->buildTransactionDTO($transaction));
    }

    public function delete($id): ?string
    {
        global $repository;
        return $repository->delete($id);
    }

    private function buildTransactionDTO($transaction): TransactionDTO
    {
        global $accountRepository, $categoryRepository, $transactionLocaleRepository;

        $transactionDTO = new TransactionDTO(
            $transaction->getId(),
            $accountRepository->findById($transaction->getAccountId()),
            $categoryRepository->findById($transaction->getCategoryId()),
            '-',
            $transaction->getValue(),
            $transaction->getDate(),
            $transaction->getType(),
            $transaction->getInstallmentsNumber(),
            $transaction->getObs()
        );

        if ($transaction->getTransactionLocaleId() != null)
            $transactionDTO->setTransactionLocale($transactionLocaleRepository->findById($transaction->getTransactionLocaleId()));

        return $transactionDTO;
    }
}
