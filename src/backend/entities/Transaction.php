<?php

class Transaction implements JsonSerializable {
    private $id;
    private $userId;
    private $accountId;
    private $categoryId;
    private $transactionLocaleId;
    private $value;
    private $date;
    private $type;
    private $installmentsNumber;
    private $obs;

    public function __construct($id, $userId, $accountId, $categoryId, $transactionLocaleId, $value, $date, $type, $installmentsNumber, $obs) {
        $this->id = $id;
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

    public function getId() {
        return $this->id;
    }

    public function setId($id) {
        $this->id = $id;
    }

    public function getUserId() {
        return $this->userId;
    }

    public function setUserId($userId) {
        $this->userId = $userId;
    }

    public function getAccountId() {
        return $this->accountId;
    }

    public function setAccountId($accountId) {
        $this->accountId = $accountId;
    }

    public function getCategoryId() {
        return $this->categoryId;
    }

    public function setCategoryId($categoryId) {
        $this->categoryId = $categoryId;
    }

    public function getTransactionLocaleId() {
        return $this->transactionLocaleId;
    }

    public function setTransactionLocaleId($transactionLocaleId) {
        $this->transactionLocaleId = $transactionLocaleId;
    }

    public function getValue() {
        return $this->value;
    }

    public function setValue($value) {
        $this->value = $value;
    }

    public function getDate() {
        return $this->date;
    }

    public function setDate($date) {
        $this->date = $date;
    }

    public function getType() {
        return $this->type;
    }

    public function setType($type) {
        $this->type = $type;
    }

    public function getInstallmentsNumber() {
        return $this->installmentsNumber;
    }

    public function setInstallmentsNumber($installmentsNumber) {
        $this->installmentsNumber = $installmentsNumber;
    }

    public function getObs() {
        return $this->obs;
    }

    public function setObs($obs) {
        $this->obs = $obs;
    }

    public function jsonSerialize(): array {
        return array(
            'id'=>$this->id,
            'userId'=>$this->userId,
            'accountId'=>$this->accountId,
            'categoryId'=>$this->categoryId,
            'transactionLocaleId'=>$this->transactionLocaleId,
            'value'=>$this->value,
            'date'=>$this->date,
            'type'=>$this->type,
            'installmentsNumber'=>$this->installmentsNumber,
            'obs'=>$this->obs
        );
    }
}
