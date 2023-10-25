<?php

include "../../backend/services/TransactionLocaleService.php";

session_start();

$service = new TransactionLocaleService();

if (array_key_exists('postTransaction', $_POST)) {
    $_SESSION['transactionLocaleId'] = "";
    header("Location: ../TransactionLocale.html");
}

if (array_key_exists('itemButton', $_POST)) {
    $_SESSION['transactionLocaleId'] = $_POST['itemButton'];
    header("Location: ../TransactionLocale.html");
}
