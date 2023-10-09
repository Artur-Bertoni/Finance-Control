<?php

include "../../backend/services/TransactionLocaleService.php";

session_start();

if (isset($_POST['cancelButton'])) {
    header("Location: ../HomePage.html");
    exit;
}

$service = new TransactionLocaleService();

$transactionLocaleId = $_POST['$transactionLocaleId'];
$userId = $_SESSION['userId'];
$name = $_POST["nameField"];
$address = $_POST["addressField"];

if ($transactionLocaleId != "") {
    $service->update($transactionLocaleId, new TransactionLocaleRequestDTO(
        $userId,
        $name,
        $address
    ));
} else {
    $service->create(new TransactionLocaleRequestDTO(
        $userId,
        $name,
        $address
    ));
}

header("Location: ../HomePage.html");
