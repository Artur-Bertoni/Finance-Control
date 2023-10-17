<?php

include "../../backend/services/TransactionService.php";

session_start();

$service = new TransactionService();

if (array_key_exists('findAllByUser', $_POST)) {
    $service->findAllByUser($_SESSION['userId']);
}

if (array_key_exists('findById', $_POST) && $_SESSION['transactionId'] != "") {
    $service->findById($_SESSION['transactionId']);
}
