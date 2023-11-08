<?php

include_once "../../backend/dto/TransferRequestDTO.php";
include_once "../../backend/dto/TransferDTO.php";
include_once "../../backend/dto/TransactionRequestDTO.php";
include_once "../../backend/services/TransactionService.php";

global $transactionService;
$transactionService = new TransactionService();

class TransferService
{
    public function create(TransferRequestDTO $requestDTO): TransferDTO|string
    {
        try {
            global $transactionService;

            $originAccountTransaction = $transactionService->create(new TransactionRequestDTO(
                $requestDTO->getUserId(),
                $requestDTO->getOriginAccountId(),
                $requestDTO->getCategoryId(),
                $requestDTO->getTransactionLocaleId(),
                $requestDTO->getValue(),
                $requestDTO->getDate(),
                'debit',
                0,
                $requestDTO->getObs()
            ));

            $destinationAccountTransaction = $transactionService->create(new TransactionRequestDTO(
                $requestDTO->getUserId(),
                $requestDTO->getDestinationAccountId(),
                $requestDTO->getCategoryId(),
                $requestDTO->getTransactionLocaleId(),
                $requestDTO->getValue(),
                $requestDTO->getDate(),
                'credit',
                0,
                $requestDTO->getObs()
            ));

            return new TransferDTO(
                $requestDTO->getUserId(),
                $originAccountTransaction->getAccountId(),
                $destinationAccountTransaction->getAccountId(),
                $requestDTO->getCategoryId(),
                $requestDTO->getTransactionLocaleId(),
                $requestDTO->getValue(),
                $requestDTO->getDate(),
                $requestDTO->getObs()
            );
        } catch (Error|Throwable $e) {
            return "Erro: " . $e->getMessage();
        }
    }
}
