<?php

class TransactionDTO implements JsonSerializable {
    private $account;
    private $category;
    private $transactionLocale;
    private $value;
    private $date;
    private $type;
    private $installmentsNumber;

    public function __construct($account, $category, $transactionLocale, $value, $date, $type, $installmentsNumber) {
        $this->account = $account;
        $this->category = $category;
        $this->transactionLocale = $transactionLocale;
        $this->value = $value;
        $this->date = $date;
        $this->type = $type;
        $this->installmentsNumber = $installmentsNumber;
    }

    public function getAccount() {
        return $this->account;
    }

    public function setAccount($account) {
        $this->account = $account;
    }

    public function getCategory() {
        return $this->category;
    }

    public function setCategory($category) {
        $this->category = $category;
    }

    public function getTransactionLocale() {
        return $this->transactionLocale;
    }

    public function setTransactionLocale($transactionLocale) {
        $this->transactionLocale = $transactionLocale;
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

    public function jsonSerialize(): array {
        return array(
            'account'=>$this->account,
            'category'=>$this->category,
            'transactionLocale'=>$this->transactionLocale,
            'value'=>$this->value,
            'date'=>$this->date,
            'type'=>$this->type,
            'installmentsNumber'=>$this->installmentsNumber,
        );
    }
}
