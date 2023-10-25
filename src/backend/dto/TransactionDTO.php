<?php

class TransactionDTO implements JsonSerializable
{
    private $id;
    private $account;
    private $category;
    private $transactionLocale;
    private $value;
    private $date;
    private $type;
    private $installmentsNumber;
    private $obs;

    public function __construct($id, $account, $category, $transactionLocale, $value, $date, $type, $installmentsNumber, $obs)
    {
        $this->id = $id;
        $this->account = $account;
        $this->category = $category;
        $this->transactionLocale = $transactionLocale;
        $this->value = $value;
        $this->date = $date;
        $this->type = $type;
        $this->installmentsNumber = $installmentsNumber;
        $this->obs = $obs;
    }

    public function getId()
    {
        return $this->id;
    }

    public function setId($id)
    {
        $this->id = $id;
    }

    public function getAccount()
    {
        return $this->account;
    }

    public function setAccount($account)
    {
        $this->account = $account;
    }

    public function getCategory()
    {
        return $this->category;
    }

    public function setCategory($category)
    {
        $this->category = $category;
    }

    public function getTransactionLocale()
    {
        return $this->transactionLocale;
    }

    public function setTransactionLocale($transactionLocale)
    {
        $this->transactionLocale = $transactionLocale;
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

    public function setObs($obs): void
    {
        $this->obs = $obs;
    }

    public function jsonSerialize(): array
    {
        return array(
            'id' => $this->id,
            'account' => $this->account,
            'category' => $this->category,
            'transactionLocale' => $this->transactionLocale,
            'value' => $this->value,
            'date' => $this->date,
            'type' => $this->type,
            'installmentsNumber' => $this->installmentsNumber,
            'obs' => $this->obs
        );
    }
}
