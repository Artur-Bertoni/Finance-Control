<?php

include "../../backend/services/AccountService.php";

session_start();

if (isset($_POST['cancelButton'])) {
    header("Location: ../HomePage.html");
    exit;
}

$service = new AccountService();

$accountId = $_POST['accountId'];
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
