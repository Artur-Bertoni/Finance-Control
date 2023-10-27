<?php

include "../../backend/services/TransactionLocaleService.php";

session_start();

$service = new TransactionLocaleService();

if (isset($_POST['homeButton'])) {
    header("Location: ../HomePage.html");
    exit;
}

if (isset($_POST['cancelButton'])) {
    header("Location: ../TransactionLocaleDashboard.html");
    exit;
}

if (isset($_POST['profileButton'])) {
    header("Location: ../User.html");
    exit;
}

if (isset($_POST['deleteButton'])) {
    $result = $service->delete($_SESSION['transactionLocaleId']);

    if ($result != null) {
        echo "<script>alert('" . $result . "');location.href=\"../TransactionLocale.html\";</script>";
        exit;
    }

    header("Location: ../TransactionLocaleDashboard.html");
    exit;
}

if (isset($_POST['saveButton'])) {
    $transactionLocaleId = $_SESSION['transactionLocaleId'];
    $userId = $_SESSION['userId'];
    $name = $_POST["nameField"];
    $address = $_POST["addressField"];

    if ($transactionLocaleId != "") {
        $result = $service->update($transactionLocaleId, new TransactionLocaleRequestDTO(
            $userId,
            $name,
            $address
        ));
    } else {
        $result = $service->create(new TransactionLocaleRequestDTO(
            $userId,
            $name,
            $address
        ));
    }

    if (!$result instanceof TransactionLocale) {
        echo "<script>alert('" . $result . "');location.href=\"../TransactionLocale.html\";</script>";
        exit;
    }

    header("Location: ../TransactionLocaleDashboard.html");
    exit;
}
