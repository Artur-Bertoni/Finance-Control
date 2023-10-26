<?php

include "../../backend/services/AccountService.php";

session_start();

$service = new AccountService();

if (array_key_exists('findAllByUser', $_POST)) {
    $service->findAllByUser($_SESSION['userId']);
}
if (array_key_exists('findById', $_POST) && $_SESSION['accountId'] != "") {
    $service->findById($_SESSION['accountId']);
}

