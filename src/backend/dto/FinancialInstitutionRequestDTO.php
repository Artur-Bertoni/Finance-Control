<?php

class FinancialInstitutionRequestDTO
{
    private $userId;
    private $name;
    private $address;
    private $contact;

    public function __construct($userId, $name, $address, $contact)
    {
        $this->userId = $userId;
        $this->name = $name;
        $this->address = $address;
        $this->contact = $contact;
    }

    public function getUserId()
    {
        return $this->userId;
    }

    public function setUserId($userId)
    {
        $this->userId = $userId;
    }

    public function getName()
    {
        return $this->name;
    }

    public function setName($name)
    {
        $this->name = $name;
    }

    public function getAddress()
    {
        return $this->address;
    }

    public function setAddress($address)
    {
        $this->address = $address;
    }

    public function getContact()
    {
        return $this->contact;
    }

    public function setContact($contact)
    {
        $this->contact = $contact;
    }
}
