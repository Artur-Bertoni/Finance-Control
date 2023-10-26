<?php

include "../../backend/services/TransactionService.php";

session_start();

$service = new TransactionService();

if (array_key_exists('postTransaction', $_POST)) {
    $_SESSION['transactionId'] = "";
    header("Location: ../Transaction.html");
    exit;
}

if (array_key_exists('transactionLocales', $_POST)) {
    header("Location: ../TransactionLocaleDashboard.html");
    exit;
}

if (array_key_exists('financialInstitutions', $_POST)) {
    header("Location: ../FinancialInstitutionDashboard.html");
    exit;
}

if (array_key_exists('accounts', $_POST)) {
    header("Location: ../AccountDashboard.html");
    exit;
}

if (array_key_exists('postCategory', $_POST)) {
    header("Location: ../Category.html");
    exit;
}

if (array_key_exists('itemButton', $_POST)) {
    $_SESSION['transactionId'] = $_POST['itemButton'];
    header("Location: ../Transaction.html");
    exit;
}
