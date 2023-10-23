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

class TransactionService {
    public function create(TransactionRequestDTO $requestDTO) {
        global $repository;
        return $repository->save($requestDTO);
    }

    public function update($id, TransactionRequestDTO $requestDTO) {
        global $repository;
        return $repository->update($id, $requestDTO);
    }

    public function findAllByUser($userId) {
        global $repository;
        $transactions = $repository->findAllByUserId($userId);
        $transactionDTOs = [];

        foreach ($transactions as $transaction) {
            $transactionDTOs[] = $this->buildTransaction($transaction);
        }

        echo json_encode($transactionDTOs);
    }

    public function findById($id) {
        global $repository;
        $transaction = $repository->findById($id);
        echo json_encode($this->buildTransaction($transaction));
    }

    private function buildTransaction($transaction): TransactionDTO {
        global $accountRepository, $categoryRepository, $transactionLocaleRepository;

        return new TransactionDTO(
            $transaction->getId(),
            $accountRepository->findById($transaction->getAccountId()),
            $categoryRepository->findById($transaction->getCategoryId()),
            $transactionLocaleRepository->findById($transaction->getTransactionLocaleId()),
            $transaction->getValue(),
            $transaction->getDate(),
            $transaction->getType(),
            $transaction->getInstallmentsNumber(),
            $transaction->getObs()
        );
    }

    public function delete($id) {
        global $repository;
        $repository->delete($id);
    }
}
