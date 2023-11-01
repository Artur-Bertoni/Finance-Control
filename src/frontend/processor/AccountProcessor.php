<?php

include "../../backend/services/AccountService.php";

session_start();

$service = new AccountService();

if (isset($_POST['homeButton'])) {
    header("Location: ../HomePage.html");
    exit;
}

if (isset($_POST['cancelButton'])) {
    header("Location: ../AccountDashboard.html");
    exit;
}

if (isset($_POST['profileButton'])) {
    header("Location: ../User.html");
    exit;
}

if (isset($_POST['deleteButton'])) {
    $result = $service->delete($_SESSION['accountId']);

    if ($result != null) {
        echo "<script>alert('" . $result . "');location.href=\"../Account.html\";</script>";
        exit;
    }

    header("Location: ../AccountDashboard.html");
    exit;
}

if (isset($_POST['saveButton'])) {
    $accountId = $_SESSION['accountId'];
    $userId = $_SESSION['userId'];
    $financialInstitutionId = $_POST['financialInstitutionField'];
    $name = $_POST["nameField"];
    $contact = $_POST["contactField"];
    $description = $_POST["descriptionField"];
    $balance = $_POST["balanceField"];

    if ($accountId != "") {
        $result = $service->update($accountId, new AccountRequestDTO(
            $userId,
            $financialInstitutionId,
            $name,
            $contact,
            $description,
            $balance
        ));
    } else {
        $result = $service->create(new AccountRequestDTO(
            $userId,
            $financialInstitutionId,
            $name,
            $contact,
            $description,
            $balance
        ));
    }

    if (!$result instanceof Account) {
        echo "<script>alert('" . $result . "');location.href=\"../Account.html\";</script>";
        exit;
    }

    header("Location: ../AccountDashboard.html");
    exit;
}
