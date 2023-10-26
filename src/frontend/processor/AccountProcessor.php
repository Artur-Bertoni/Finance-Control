<?php

include "../../backend/services/AccountService.php";

session_start();

$service = new AccountService();

if (isset($_POST['homeButton']) || isset($_POST['cancelButton'])) {
    header("Location: ../HomePage.html");
    exit;
}

if (isset($_POST['profileButton'])) {
    header("Location: ../User.html");
    exit;
}

$accountId = $_SESSION['accountId'];
$userId = $_SESSION['userId'];
$financialInstitutionId = $_POST['financialInstitutionField'];
$name = $_POST["nameField"];
$contact = $_POST["contactField"];
$description = $_POST["descriptionField"];

if ($accountId != "") {
    $service->update($accountId, new AccountRequestDTO(
        $userId,
        $financialInstitutionId,
        $name,
        $contact,
        $description
    ));
} else {
    $service->create(new AccountRequestDTO(
        $userId,
        $financialInstitutionId,
        $name,
        $contact,
        $description
    ));
}

header("Location: ../HomePage.html");
