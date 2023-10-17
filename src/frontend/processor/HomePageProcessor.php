<?php

include "../../backend/services/TransactionService.php";

session_start();

$service = new TransactionService();

if (array_key_exists('postTransaction', $_POST)) {
    $_SESSION['transactionId'] = "";
    header("Location: ../Transaction.html");
}

if (array_key_exists('postTransactionLocale', $_POST)) {
    header("Location: ../TransactionLocale.html");
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
    //TODO remover adaptar para pegar o id do item clicado, ao invés do último criado
    $_SESSION['transactionId'] = $_POST['transactionId'];
    header("Location: ../Transaction.html");
}
