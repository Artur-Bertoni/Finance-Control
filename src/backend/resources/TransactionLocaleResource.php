<?php

include "../../backend/services/TransactionLocaleService.php";

session_start();

$service = new TransactionLocaleService();

if (array_key_exists('findAllByUser', $_POST)) {
    $service->findAllByUser($_SESSION['userId']);
}

if (array_key_exists('findById', $_POST) && $_SESSION['transactionLocaleId'] != "") {
    $service->findById($_SESSION['transactionLocaleId']);
}
