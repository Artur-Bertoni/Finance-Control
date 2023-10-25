<?php

include "../../backend/services/TransactionService.php";

session_start();

$service = new TransactionService();

if (array_key_exists('postTransaction', $_POST)) {
    $_SESSION['transactionId'] = "";
    header("Location: ../Transaction.html");
}

if (array_key_exists('transactionLocales', $_POST)) {
    header("Location: ../TransactionLocaleDashboard.html");
}

if (array_key_exists('postFinancialInstitution', $_POST)) {
    header("Location: ../FinancialInstitution.html");
}

if (array_key_exists('postAccount', $_POST)) {
    header("Location: ../Account.html");
}

if (array_key_exists('postCategory', $_POST)) {
    header("Location: ../Category.html");
}

if (array_key_exists('itemButton', $_POST)) {
    $_SESSION['transactionId'] = $_POST['itemButton'];
    header("Location: ../Transaction.html");
}
