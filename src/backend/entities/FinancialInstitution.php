<?php

class FinancialInstitution implements JsonSerializable
{
    private $id;
    private $userId;
    private $name;
    private $address;
    private $contact;

    public function __construct($id, $userId, $name, $address, $contact)
    {
        $this->id = $id;
        $this->userId = $userId;
        $this->name = $name;
        $this->address = $address;
        $this->contact = $contact;
    }

    public function getId()
    {
        return $this->id;
    }

    public function setId($id)
    {
        $this->id = $id;
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

    public function jsonSerialize()
    {
        return array(
            'id' => $this->id,
            'userId' => $this->userId,
            'name' => $this->name,
            'address' => $this->address,
            'contact' => $this->contact
        );
    }
}
