<?php

class TransactionRequestDTO
{
    private $userId;
    private $accountId;
    private $categoryId;
    private $transactionLocaleId;
    private $value;
    private $date;
    private $type;
    private $installmentsNumber;
    private $obs;

    public function __construct($userId, $accountId, $categoryId, $transactionLocaleId, $value, $date, $type, $installmentsNumber, $obs)
    {
        $this->userId = $userId;
        $this->accountId = $accountId;
        $this->categoryId = $categoryId;
        $this->transactionLocaleId = $transactionLocaleId;
        $this->value = $value;
        $this->date = $date;
        $this->type = $type;
        $this->installmentsNumber = $installmentsNumber;
        $this->obs = $obs;
    }

    public function getUserId()
    {
        return $this->userId;
    }

    public function setUserId($userId)
    {
        $this->userId = $userId;
    }

    public function getAccountId()
    {
        return $this->accountId;
    }

    public function setAccountId($accountId)
    {
        $this->accountId = $accountId;
    }

    public function getCategoryId()
    {
        return $this->categoryId;
    }

    public function setCategoryId($categoryId)
    {
        $this->categoryId = $categoryId;
    }

    public function getTransactionLocaleId()
    {
        return $this->transactionLocaleId;
    }

    public function setTransactionLocaleId($transactionLocaleId)
    {
        $this->transactionLocaleId = $transactionLocaleId;
    }

    public function getValue()
    {
        return $this->value;
    }

    public function setValue($value)
    {
        $this->value = $value;
    }

    public function getDate()
    {
        return $this->date;
    }

    public function setDate($date)
    {
        $this->date = $date;
    }

    public function getType()
    {
        return $this->type;
    }

    public function setType($type)
    {
        $this->type = $type;
    }

    public function getInstallmentsNumber()
    {
        return $this->installmentsNumber;
    }

    public function setInstallmentsNumber($installmentsNumber)
    {
        $this->installmentsNumber = $installmentsNumber;
    }

    public function getObs()
    {
        return $this->obs;
    }

    public function setObs($obs)
    {
        $this->obs = $obs;
    }
}
