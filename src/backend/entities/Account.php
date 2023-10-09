<?php

class Account implements JsonSerializable {
    private $id;
    private $userId;
    private $financialInstitutionId;
    private $name;
    private $contact;
    private $description;

    public function __construct($id, $userId, $financialInstitutionId, $name, $contact, $description) {
        $this->id = $id;
        $this->userId = $userId;
        $this->financialInstitutionId = $financialInstitutionId;
        $this->name = $name;
        $this->contact = $contact;
        $this->description = $description;
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

    public function jsonSerialize() {
        return array(
            'id'=>$this->id,
            'userId'=>$this->userId,
            'financialInstitutionId'=>$this->financialInstitutionId,
            'name'=>$this->name,
            'contact'=>$this->contact,
            'description'=>$this->description
        );
    }
}
