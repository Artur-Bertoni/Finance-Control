<?php

class FinancialInstitutionDTO implements JsonSerializable {
    private $id;
    private $name;
    private $address;
    private $contact;

    public function __construct($id, $name, $address, $contact) {
        $this->id = $id;
        $this->name = $name;
        $this->address = $address;
        $this->contact = $contact;
    }

    public function getId() {
        return $this->id;
    }

    public function setId($id) {
        $this->id = $id;
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

    public function getContact() {
        return $this->contact;
    }

    public function setContact($contact) {
        $this->contact = $contact;
    }

    public function jsonSerialize() {
        return array(
            'id'=>$this->id,
            'name'=>$this->name,
            'address'=>$this->address,
            'contact'=>$this->contact
        );
    }
}
