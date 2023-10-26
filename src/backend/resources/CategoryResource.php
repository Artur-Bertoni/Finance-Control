<?php

include "../../backend/services/CategoryService.php";

session_start();

$service = new CategoryService();

if (array_key_exists('findAllByUser', $_POST)) {
    $service->findAllByUser($_SESSION['userId']);
}

if (array_key_exists('findById', $_POST) && $_SESSION['categoryId'] != "") {
    $service->findById($_SESSION['categoryId']);
}
