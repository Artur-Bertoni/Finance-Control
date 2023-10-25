<?php

include "../../backend/services/TransactionLocaleService.php";

session_start();

$service = new TransactionLocaleService();

if (isset($_POST['cancelButton'])) {
    header("Location: ../HomePage.html");
    exit;
}

if (isset($_POST['deleteButton'])) {
    $service->delete($_SESSION['transactionLocaleId']);
    header("Location: ../HomePage.html");
    exit;
}

$transactionLocaleId = $_SESSION['transactionLocaleId'];
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

header("Location: ../TransactionLocaleDashboard.html");
