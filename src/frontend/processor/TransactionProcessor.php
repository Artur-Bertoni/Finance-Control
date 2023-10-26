<?php

include "../../backend/services/TransactionService.php";

session_start();

$service = new TransactionService();

if (isset($_POST['homeButton']) || isset($_POST['cancelButton'])) {
    header("Location: ../HomePage.html");
    exit;
}

if (isset($_POST['profileButton'])) {
    header("Location: ../User.html");
    exit;
}

if (isset($_POST['deleteButton'])) {
    $service->delete($_SESSION['transactionId']);
    header("Location: ../HomePage.html");
    exit;
}

$transactionId = $_SESSION['transactionId'];
$userId = $_SESSION['userId'];
$accountId = $_POST["accountField"];
$categoryId = $_POST["categoryField"];
$transactionLocaleId = $_POST["transactionLocaleField"];
$value = $_POST["valueField"];
$date = $_POST["dateField"];
$type = $_POST["typeRadio"];
$installmentsNumber = $_POST["installmentsNumberField"];
$obs = $_POST["obsField"];

if ($transactionId != "") {
    $service->update($transactionId, new TransactionRequestDTO(
        $userId,
        $accountId,
        $categoryId,
        $transactionLocaleId,
        $value,
        $date,
        $type,
        $installmentsNumber,
        $obs
    ));
} else {
    $service->create(new TransactionRequestDTO(
        $userId,
        $accountId,
        $categoryId,
        $transactionLocaleId,
        $value,
        $date,
        $type,
        $installmentsNumber,
        $obs
    ));
}

header("Location: ../HomePage.html");
