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
        global $repository, $accountRepository;

        if ($requestDTO->getType() === 'credit')
            $accountRepository->patchBalance($requestDTO->getAccountId(), $requestDTO->getValue());
        else
            $accountRepository->patchBalance($requestDTO->getAccountId(), -$requestDTO->getValue());

        return $repository->save($requestDTO);
    }

    public function update($id, TransactionRequestDTO $requestDTO): Transaction|string
    {
        global $repository, $accountRepository;

        if ($requestDTO->getType() === 'credit')
            $accountRepository->patchBalance($requestDTO->getAccountId(),
                -($repository->findById($id)->getValue() - $requestDTO->getValue()));
        else
            $accountRepository->patchBalance($requestDTO->getAccountId(),
                ($repository->findById($id)->getValue() - $requestDTO->getValue()));

        $result = $repository->update($id, $requestDTO);

        if ($requestDTO->getTransferPartnerId() != 0) {
            $partner = $repository->findById($requestDTO->getTransferPartnerId());

            if ($partner->getValue() != $requestDTO->getValue()) {
                $requestDTO->setTransferPartnerId($id);
                $requestDTO->setAccountId($partner->getAccountId());

                if ($requestDTO->getType() === 'debit')
                    $requestDTO->setType('credit');
                else
                    $requestDTO->setType('debit');

                $this->update($partner->getId(), $requestDTO);
            }
        }

        return $result;
    }

    public function findAllByUser($userId, $startDate, $endDate, $categoryId, $accountId): void
    {
        global $repository;
        $transactions = $repository->findAllByUserId($userId, $startDate, $endDate, $categoryId, $accountId);
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
        global $repository, $accountRepository;

        $type = $repository->findById($id)->getType();

        if ($type === 'credit')
            $accountRepository->patchBalance($repository->findById($id)->getAccountId(), -$repository->findById($id)->getValue());
        else
            $accountRepository->patchBalance($repository->findById($id)->getAccountId(), $repository->findById($id)->getValue());

        if ($repository->findById($id)->getTransferPartnerId() != 0) {
            $partner = $repository->findById($repository->findById($id)->getTransferPartnerId());

            $result = $repository->delete($id);

            if ($partner instanceof  Transaction)
                $this->delete($partner->getId());
        } else
            $result = $repository->delete($id);

        return $result;
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
            $transaction->getObs(),
            $transaction->getTransferPartnerId()
        );

        if ($transaction->getTransactionLocaleId() != null)
            $transactionDTO->setTransactionLocale($transactionLocaleRepository->findById($transaction->getTransactionLocaleId()));

        return $transactionDTO;
    }

    public function patchTransferPartner($id, $transferPartnerId): Transaction|string
    {
        global $repository;

        return $repository->patchTransferPartner($id, $transferPartnerId);
    }
}
