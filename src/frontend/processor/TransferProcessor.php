<?php

include "../../backend/services/TransferService.php";

session_start();

$service = new TransferService();

if (isset($_POST['homeButton']) || isset($_POST['cancelButton'])) {
    header("Location: ../AccountDashboard.html");
    exit;
}

if (isset($_POST['profileButton'])) {
    header("Location: ../User.html");
    exit;
}

if (isset($_POST['saveButton'])) {
    $userId = $_SESSION['userId'];
    $originAccountId = $_POST["originAccountField"];
    $destinationAccountId = $_POST["destinationAccountField"];
    $categoryId = $_POST["categoryField"];
    $transactionLocaleId = $_POST["transactionLocaleField"];
    $value = $_POST["valueField"];
    $date = $_POST["dateField"];
    $type = $_POST["typeRadio"];
    $obs = $_POST["obsField"];

    $result = $service->create(new TransferRequestDTO(
        $userId,
        $originAccountId,
        $destinationAccountId,
        $categoryId,
        $transactionLocaleId,
        $value,
        $date,
        $type,
        $obs
    ));

    if (!$result instanceof TransferDTO) {
        echo "<script>alert('" . $result . "');location.href=\"../Transfer.html\";</script>";
        exit;
    }

    header("Location: ../HomePage.html");
    exit;
}
