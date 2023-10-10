<?php

include_once "../../backend/repository/TransactionRepository.php";
include_once "../../backend/repository/AccountRepository.php";
include_once "../../backend/repository/TransactionLocaleRepository.php";
include_once "../../backend/repository/CategoryRepository.php";
include_once "../../backend/dto/TransactionRequestDTO.php";

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
        global $repository, $accountRepository, $categoryRepository, $transactionLocaleRepository;
        $transactions = $repository->findAllByUserId($userId);
        $transactionDTOs = [];

        foreach ($transactions as $transaction) {
            $accountId = $transaction->getAccountId();
            $categoryId = $transaction->getCategoryId();
            $transactionLocaleId = $transaction->getTransactionLocaleId();

            $account = $accountRepository->findById($accountId);
            $category = $categoryRepository->findById($categoryId);
            $transactionLocale = $transactionLocaleRepository->findById($transactionLocaleId);

            $transactionDTO = new TransactionDTO(
                $account,
                $category,
                $transactionLocale,
                $transaction->getValue(),
                $transaction->getDate(),
                $transaction->getType(),
                $transaction->getInstallmentsNumber()
            );

            $transactionDTOs[] = $transactionDTO;
        }

        echo json_encode(['result' => $transactionDTOs]);
    }
}
