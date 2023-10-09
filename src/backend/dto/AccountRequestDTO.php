<?php

class AccountRequestDTO {
    private $userId;
    private $financialInstitutionId;
    private $name;
    private $contact;
    private $description;

    public function __construct($userId, $financialInstitutionId, $name, $contact, $description) {
        $this->userId = $userId;
        $this->financialInstitutionId = $financialInstitutionId;
        $this->name = $name;
        $this->contact = $contact;
        $this->description = $description;
    }

    public function getUserId() {
        return $this->userId;
    }

    public function setUserId($userId) {
        $this->userId = $userId;
    }

    public function getFinancialInstitutionId() {
        return $this->financialInstitutionId;
    }

    public function setFinancialInstitutionId($financialInstitutionId) {
        $this->financialInstitutionId = $financialInstitutionId;
    }

    public function getName() {
        return $this->name;
    }

    public function setName($name) {
        $this->name = $name;
    }

    public function getDescription() {
        return $this->description;
    }

    public function setDescription($description) {
        $this->description = $description;
    }

    public function getContact() {
        return $this->contact;
    }

    public function setContact($contact) {
        $this->contact = $contact;
    }
}
