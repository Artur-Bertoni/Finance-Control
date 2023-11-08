<?php

class TransferRequestDTO implements JsonSerializable
{
    private $userId;
    private $originAccountId;
    private $destinationAccountId;
    private $categoryId;
    private $transactionLocaleId;
    private $value;
    private $date;
    private $obs;

    public function __construct($userId, $originAccountId, $destinationAccountId, $categoryId, $transactionLocaleId, $value, $date, $obs)
    {
        $this->userId = $userId;
        $this->originAccountId = $originAccountId;
        $this->destinationAccountId = $destinationAccountId;
        $this->categoryId = $categoryId;
        $this->transactionLocaleId = $transactionLocaleId;
        $this->value = $value;
        $this->date = $date;
        $this->obs = $obs;
    }

    public function getUserId()
    {
        return $this->userId;
    }

    public function setUserId($userId): void
    {
        $this->userId = $userId;
    }

    public function getOriginAccountId()
    {
        return $this->originAccountId;
    }

    public function setOriginAccountId($originAccountId): void
    {
        $this->originAccountId = $originAccountId;
    }

    public function getDestinationAccountId()
    {
        return $this->destinationAccountId;
    }

    public function setDestinationAccountId($destinationAccountId): void
    {
        $this->destinationAccountId = $destinationAccountId;
    }

    public function getCategoryId()
    {
        return $this->categoryId;
    }

    public function setCategoryId($categoryId): void
    {
        $this->categoryId = $categoryId;
    }

    public function getTransactionLocaleId()
    {
        return $this->transactionLocaleId;
    }

    public function setTransactionLocaleId($transactionLocaleId): void
    {
        $this->transactionLocaleId = $transactionLocaleId;
    }

    public function getValue()
    {
        return $this->value;
    }

    public function setValue($value): void
    {
        $this->value = $value;
    }

    public function getDate()
    {
        return $this->date;
    }

    public function setDate($date): void
    {
        $this->date = $date;
    }

    public function getObs()
    {
        return $this->obs;
    }

    public function setObs($obs): void
    {
        $this->obs = $obs;
    }

    public function jsonSerialize(): array
    {
        return array(
            'userId' => $this->userId,
            'originAccountId' => $this->originAccountId,
            'destinationAccountId' => $this->destinationAccountId,
            'categoryId' => $this->categoryId,
            'transactionLocaleId' => $this->transactionLocaleId,
            'value' => $this->value,
            'date' => $this->date,
            'obs' => $this->obs
        );
    }
}
