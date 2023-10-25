<?php

class AccountDTO implements JsonSerializable
{
    private $id;
    private $financialInstitution;
    private $name;
    private $contact;
    private $description;

    public function __construct($id, $financialInstitution, $name, $contact, $description)
    {
        $this->id = $id;
        $this->financialInstitution = $financialInstitution;
        $this->name = $name;
        $this->contact = $contact;
        $this->description = $description;
    }

    public function getId()
    {
        return $this->id;
    }

    public function setId($id)
    {
        $this->id = $id;
    }

    public function getFinancialInstitution()
    {
        return $this->financialInstitution;
    }

    public function setFinancialInstitution($financialInstitution)
    {
        $this->financialInstitution = $financialInstitution;
    }

    public function getName()
    {
        return $this->name;
    }

    public function setName($name)
    {
        $this->name = $name;
    }

    public function getDescription()
    {
        return $this->description;
    }

    public function setDescription($description)
    {
        $this->description = $description;
    }

    public function getContact()
    {
        return $this->contact;
    }

    public function setContact($contact)
    {
        $this->contact = $contact;
    }

    public function jsonSerialize(): array
    {
        return array(
            'id' => $this->id,
            'financialInstitution' => $this->financialInstitution,
            'name' => $this->name,
            'contact' => $this->contact,
            'description' => $this->description
        );
    }
}
