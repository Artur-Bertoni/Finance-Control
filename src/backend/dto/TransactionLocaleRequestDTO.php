<?php

class TransactionLocaleRequestDTO {
    private $userId;
    private $name;
    private $address;

    public function __construct($userId, $name, $address) {
        $this->userId = $userId;
        $this->name = $name;
        $this->address = $address;
    }

    public function getUserId() {
        return $this->userId;
    }

    public function setUserId($userId) {
        $this->userId = $userId;
    }

    public function getName() {
        return $this->name;
    }

    public function setName($name) {
        $this->name = $name;
    }

    public function getAddress() {
        return $this->address;
    }

    public function setAddress($address) {
        $this->address = $address;
    }
}
