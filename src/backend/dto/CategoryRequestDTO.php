<?php

class CategoryRequestDTO {
    private $userId;
    private $name;
    private $description;

    public function __construct($userId, $name, $description) {
        $this->userId = $userId;
        $this->name = $name;
        $this->description = $description;
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

    public function getDescription() {
        return $this->description;
    }

    public function setDescription($description) {
        $this->description = $description;
    }
}
